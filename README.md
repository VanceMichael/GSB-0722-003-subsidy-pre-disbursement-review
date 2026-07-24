# 就业补贴拨付前复核服务（GSB-0722-003）

一套本地运行的就业补贴「拨付前复核」后端服务。支持登记补贴申请，并在申请「复审通过但尚未拨付」时发起复核；复核可通过或退回，退回后申请进入可修正状态，全过程留下完整流转记录。

- 技术栈：Spring Boot 3.2 · Spring Data JPA · H2（内存库）· Lombok · Bean Validation · Maven
- 全部数据本地运行，不依赖任何外部服务
- 分层：Entity → Repository → Service → DTO → Controller，控制器不含持久化逻辑
- 金额使用 `BigDecimal`，状态使用枚举

## 领域模型

### 申请状态 `ApplicationStatus`

| 枚举 | 含义 | 能否发起复核 |
|------|------|--------------|
| `REGISTERED` | 已登记 | 否 |
| `RE_REVIEW_PASSED` | 复审通过（待拨付） | ✅ 是 |
| `UNDER_REVIEW` | 复核中（存在未关闭复核） | 否 |
| `CORRECTABLE` | 可修正（复核退回后） | 否 |
| `APPROVED_FOR_DISBURSEMENT` | 复核通过待拨付 | 否 |
| `DISBURSED` | 已拨付 | 否 |
| `RE_REVIEW_REJECTED` | 复审未通过 | 否 |

### 其他枚举
- 风险等级 `RiskLevel`：`LOW` / `MEDIUM` / `HIGH`
- 复核结论 `ReviewConclusion`：`APPROVED`（通过）/ `RETURNED`（退回）
- 复核状态 `ReviewStatus`：`OPEN`（未关闭）/ `CLOSED`（已关闭）

### 核心业务规则
1. 只有 **复审通过（`RE_REVIEW_PASSED`）** 的申请可以发起复核。
2. **已拨付（`DISBURSED`）** 和 **复审未通过（`RE_REVIEW_REJECTED`）** 的申请不能发起复核。
3. 一个申请 **同时只能有一个未关闭（`OPEN`）的复核**。
4. 复核 **退回** 后，申请进入 **可修正（`CORRECTABLE`）**；复核 **通过** 后进入 **复核通过待拨付**。
5. 每次状态变化都会写入一笔 `FlowRecord` 流转记录（动作、前后状态、操作人、时间、备注）。

### 完整生命周期

新登记的申请不再停留在「已登记」，可通过复审接口推进：

```
登记 REGISTERED
  └─(复审通过)→ RE_REVIEW_PASSED ─(发起复核)→ UNDER_REVIEW
                                                   ├─(退回)→ CORRECTABLE ─(重新复审)→ RE_REVIEW_PASSED …（可循环）
                                                   └─(通过)→ APPROVED_FOR_DISBURSEMENT ─(拨付)→ DISBURSED
  └─(复审未通过)→ RE_REVIEW_REJECTED（终态）
```

- 复审入口接受「已登记」与「可修正」两种状态，因此退回后的申请修正材料后可重新复审，形成闭环。
- 「已拨付」「复审未通过」为不可再发起复核的终态。

## 接口清单

基础地址：`http://localhost:8080`

| 方法 | 路径 | 说明 | 状态前提 | 成功码 |
|------|------|------|----------|--------|
| POST | `/api/applications` | 登记申请 | 无 | 201 |
| GET | `/api/applications` | 查询全部申请 | 无 | 200 |
| GET | `/api/applications/{id}` | 查询单个申请 | 申请须存在 | 200 |
| POST | `/api/applications/{id}/re-review` | 复审（通过/未通过） | 申请状态为 `REGISTERED` 或 `CORRECTABLE` | 200 |
| POST | `/api/applications/{id}/disbursement` | 拨付 | 申请状态为 `APPROVED_FOR_DISBURSEMENT` | 200 |
| GET | `/api/applications/{id}/reviews` | 查询申请的复核列表 | 申请须存在 | 200 |
| GET | `/api/applications/{id}/flow-records` | 查询申请的流转记录 | 申请须存在 | 200 |
| POST | `/api/applications/{applicationId}/reviews` | 发起复核 | 申请状态为 `RE_REVIEW_PASSED` 且当前无 `OPEN` 复核 | 201 |
| POST | `/api/reviews/{reviewId}/decision` | 做出复核结论（通过/退回） | 复核状态为 `OPEN` | 200 |

错误码：参数校验失败 `400`；资源不存在 `404`；违反业务规则（状态前提不满足、已存在未关闭复核、复核已关闭）`409`。

### 请求/响应示例

**登记申请**
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{"employerName":"阳光科技有限公司","subsidyPeriod":"2026-Q1","appliedAmount":120000.00}'
```

**复审通过**（申请状态须为已登记或可修正；`passed=false` 则复审未通过）
```bash
curl -X POST http://localhost:8080/api/applications/1/re-review \
  -H "Content-Type: application/json" \
  -d '{"passed":true,"operator":"复审员赵六","remark":"初审无误"}'
```

**发起复核**（applicationId 需处于复审通过状态）
```bash
curl -X POST http://localhost:8080/api/applications/1/reviews \
  -H "Content-Type: application/json" \
  -d '{"riskLevel":"HIGH","problemDescription":"申报金额与参保人数不匹配","handler":"复核员张三"}'
```

**复核退回**（reviewId 需为 OPEN）
```bash
curl -X POST http://localhost:8080/api/reviews/1/decision \
  -H "Content-Type: application/json" \
  -d '{"conclusion":"RETURNED","handler":"复核员张三","remark":"材料不全，退回补充"}'
```

**复核通过**
```bash
curl -X POST http://localhost:8080/api/reviews/1/decision \
  -H "Content-Type: application/json" \
  -d '{"conclusion":"APPROVED","handler":"复核员张三","remark":"核查无异常，通过"}'
```

**拨付**（申请状态须为复核通过待拨付）
```bash
curl -X POST http://localhost:8080/api/applications/1/disbursement \
  -H "Content-Type: application/json" \
  -d '{"operator":"出纳王七","remark":"完成拨付"}'
```

**查看流转记录**
```bash
curl http://localhost:8080/api/applications/1/flow-records
```

## 样例数据

服务启动时自动写入 4 条样例申请（见 `DataSeeder`），覆盖不同状态前提：

| id | 单位 | 状态 | 用于演示 |
|----|------|------|----------|
| 1 | 阳光科技有限公司 | `RE_REVIEW_PASSED` | 可成功发起复核 |
| 2 | 恒信物流有限公司 | `REGISTERED` | 尚未复审通过，发起复核返回 409 |
| 3 | 大地建筑集团 | `DISBURSED` | 已拨付，发起复核返回 409 |
| 4 | 未来教育科技 | `RE_REVIEW_REJECTED` | 复审未通过，发起复核返回 409 |

## 运行命令

> 本机默认 `mvn` 使用 Java 25，本项目要求 Java 17。以下命令显式指定 `JAVA_HOME` 指向 JDK 17。

**运行测试**
```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home \
  mvn test
```

**启动服务**
```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home \
  mvn spring-boot:run
```

**打包并运行 jar**
```bash
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home \
  mvn clean package
java -jar target/subsidy-pre-disbursement-review-0.0.1-SNAPSHOT.jar
```

启动后：
- 服务地址：http://localhost:8080
- H2 控制台：http://localhost:8080/h2-console （JDBC URL：`jdbc:h2:mem:subsidydb`，用户名 `sa`，空密码）

## 自动化测试

- `ReviewServiceTest`：服务层业务规则（发起前提、唯一未关闭复核、退回/通过后的状态与流转记录、重复处理拦截、复审推进、退回后重新复审闭环、拨付前提、从登记到拨付的完整流程）。
- `ApplicationControllerTest`：接口层（登记、参数校验 400、发起复核 201/409、完整退回流程、经 HTTP 从登记走到拨付的端到端流程、非法状态复审 409）。

共 20 个用例，`mvn test` 全部通过。
