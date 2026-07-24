# 就业补贴拨付前复核服务 - 接口文档

## 运行命令

```bash
# 编译并运行测试
mvn clean test

# 启动应用（默认端口 8080，H2 内存数据库，启动时自动加载样例数据）
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
java -jar target/subsidy-review-1.0.0.jar
```

> **注意**：如本机默认 JDK 不是 17，请显式指定：
> ```bash
> JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
> ```

启动后可访问：
- 服务地址：`http://localhost:8080`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL: `jdbc:h2:mem:subsidy`，用户名 `sa`，密码为空）

---

## 状态机

### 申请状态（ApplicationStatus）

| 状态 | 含义 |
|------|------|
| `REGISTERED` | 已登记 |
| `UNDER_REVIEW` | 复审中 |
| `REVIEW_APPROVED` | 复审通过（待拨付，可发起拨付前复核） |
| `PRE_DISBURSEMENT_REVIEW` | 拨付前复核中 |
| `RETURNED_FOR_CORRECTION` | 复核退回（可修正） |
| `DISBURSED` | 已拨付 |
| `REVIEW_REJECTED` | 复审未通过 |

### 状态流转

```
REGISTERED ──submit──▶ UNDER_REVIEW ──approve──▶ REVIEW_APPROVED
                            │                          │
                          reject                  initiateReview
                            │                          ▼
                            ▼               PRE_DISBURSEMENT_REVIEW
                    REVIEW_REJECTED                │
                                               ┌───┴───┐
                                            close(PASS) close(RETURN)
                                               │          │
                                               ▼          ▼
                                        REVIEW_APPROVED  RETURNED_FOR_CORRECTION
                                               │          │
                                            disburse   resubmit
                                               │          │
                                               ▼          ▼
                                          DISBURSED  REVIEW_APPROVED ──▶ disburse ──▶ DISBURSED
```

### 复核状态（ReviewStatus）

| 状态 | 含义 |
|------|------|
| `OPEN` | 未关闭（进行中） |
| `CLOSED` | 已关闭 |

### 风险等级（RiskLevel）

`LOW`（低）、`MEDIUM`（中）、`HIGH`（高）

### 处理结论（ReviewConclusion）

| 结论 | 申请状态变化 |
|------|-------------|
| `PASS` | 复核通过，回到 `REVIEW_APPROVED` |
| `RETURN` | 复核退回，进入 `RETURNED_FOR_CORRECTION` |

---

## 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

- `code=0` 表示成功；非 0 为业务/校验错误。
- HTTP 状态码：`200` 成功，`400` 业务/校验错误，`404` 资源不存在。

---

## 一、申请接口

### 1. 登记申请

- **POST** `/api/applications`
- **状态前提**：无（新建即为 `REGISTERED`）
- **请求体**：

```json
{
  "unitName": "示例科技有限公司",
  "subsidyPeriod": "2024-Q1",
  "amount": 50000.00
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| unitName | String | 是 | 单位名称 |
| subsidyPeriod | String | 是 | 补贴周期 |
| amount | BigDecimal | 是 | 申请金额（>0） |

- **响应**：返回创建的 `ApplicationDTO`，状态为 `REGISTERED`。

---

### 2. 查询申请列表

- **GET** `/api/applications`
- **状态前提**：无
- **响应**：返回 `ApplicationDTO[]`。

---

### 3. 查询单个申请

- **GET** `/api/applications/{id}`
- **状态前提**：无
- **路径参数**：`id` - 申请ID
- **响应**：返回 `ApplicationDTO`。

---

### 4. 查询申请流转记录

- **GET** `/api/applications/{id}/flow-records`
- **状态前提**：无
- **响应**：返回 `FlowRecordDTO[]`，按时间正序排列。

---

### 5. 提交复审

- **POST** `/api/applications/{id}/submit`
- **状态前提**：申请当前状态必须为 `REGISTERED`
- **请求体**：

```json
{
  "operator": "张三",
  "remark": "材料齐全，提交复审"
}
```

- **结果**：状态 `REGISTERED → UNDER_REVIEW`

---

### 6. 复审通过

- **POST** `/api/applications/{id}/approve`
- **状态前提**：申请当前状态必须为 `UNDER_REVIEW`
- **请求体**：同（operator + remark）
- **结果**：状态 `UNDER_REVIEW → REVIEW_APPROVED`

---

### 7. 复审不通过

- **POST** `/api/applications/{id}/reject`
- **状态前提**：申请当前状态必须为 `UNDER_REVIEW`
- **请求体**：同（operator + remark）
- **结果**：状态 `UNDER_REVIEW → REVIEW_REJECTED`（终态，不可发起复核）

---

### 8. 修正后重新提交

- **POST** `/api/applications/{id}/resubmit`
- **状态前提**：申请当前状态必须为 `RETURNED_FOR_CORRECTION`
- **请求体**：同（operator + remark）
- **结果**：状态 `RETURNED_FOR_CORRECTION → REVIEW_APPROVED`

---

### 9. 拨付

- **POST** `/api/applications/{id}/disburse`
- **状态前提**：申请当前状态必须为 `REVIEW_APPROVED`
- **请求体**：同（operator + remark）
- **结果**：状态 `REVIEW_APPROVED → DISBURSED`（终态，不可发起复核）

---

## 二、拨付前复核接口

### 1. 发起复核

- **POST** `/api/reviews/application/{applicationId}`
- **状态前提**：
  - 申请状态必须为 `REVIEW_APPROVED`（复审通过、尚未拨付）
  - 同一申请**不能存在**未关闭（`OPEN`）的复核
  - 状态为 `DISBURSED` 或 `REVIEW_REJECTED` 的申请**不能**发起复核
- **请求体**：

```json
{
  "riskLevel": "MEDIUM",
  "problemDescription": "部分员工社保缴纳月数不足",
  "processor": "王五"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| riskLevel | RiskLevel | 是 | 风险等级：LOW / MEDIUM / HIGH |
| problemDescription | String | 否 | 问题描述 |
| processor | String | 是 | 处理人 |

- **结果**：
  - 创建一条 `OPEN` 状态的复核记录
  - 申请状态 `REVIEW_APPROVED → PRE_DISBURSEMENT_REVIEW`
  - 新增一条流转记录

---

### 2. 关闭复核（给出处理结论）

- **POST** `/api/reviews/{reviewId}/close`
- **状态前提**：复核记录存在且状态为 `OPEN`（已关闭的不能重复关闭）
- **请求体**：

```json
{
  "conclusion": "RETURN",
  "problemDescription": "需补充社保缴纳证明材料",
  "processor": "王五"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conclusion | ReviewConclusion | 是 | 处理结论：PASS / RETURN |
| problemDescription | String | 否 | 补充/更新问题描述 |
| processor | String | 是 | 处理人 |

- **结果**：
  - 复核状态变为 `CLOSED`，记录 `closedAt` 时间
  - `PASS` → 申请状态 `PRE_DISBURSEMENT_REVIEW → REVIEW_APPROVED`
  - `RETURN` → 申请状态 `PRE_DISBURSEMENT_REVIEW → RETURNED_FOR_CORRECTION`（进入可修正状态）
  - 新增一条流转记录

---

### 3. 查询单条复核记录

- **GET** `/api/reviews/{id}`
- **状态前提**：无
- **响应**：返回 `ReviewDTO`。

---

### 4. 查询申请的所有复核记录

- **GET** `/api/reviews/application/{applicationId}`
- **状态前提**：无
- **响应**：返回 `ReviewDTO[]`，按创建时间倒序。

---

## 三、DTO 结构说明

### ApplicationDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| unitName | String | 单位名称 |
| subsidyPeriod | String | 补贴周期 |
| amount | BigDecimal | 申请金额 |
| status | ApplicationStatus | 当前状态枚举 |
| statusDescription | String | 状态中文描述 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### ReviewDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| applicationId | Long | 关联申请ID |
| riskLevel | RiskLevel | 风险等级 |
| riskLevelDescription | String | 风险等级中文描述 |
| problemDescription | String | 问题描述 |
| conclusion | ReviewConclusion | 处理结论（关闭后有值） |
| conclusionDescription | String | 结论中文描述 |
| processor | String | 处理人 |
| status | ReviewStatus | 复核状态 |
| statusDescription | String | 复核状态中文描述 |
| createdAt | LocalDateTime | 创建时间 |
| closedAt | LocalDateTime | 关闭时间 |

### FlowRecordDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| fromStatus | ApplicationStatus | 变更前状态（首次登记为 null） |
| fromStatusDescription | String | 变更前状态描述 |
| toStatus | ApplicationStatus | 变更后状态 |
| toStatusDescription | String | 变更后状态描述 |
| action | String | 动作名称 |
| remark | String | 备注 |
| operator | String | 操作人 |
| createdAt | LocalDateTime | 记录时间 |

---

## 四、样例数据

启动时 [DataInitializer](file:///Users/vance/project/mineProject/gsb0722/source/GSB-0722-003-subsidy-pre-disbursement-review/Squirtle/src/main/java/com/example/subsidy/config/DataInitializer.java) 会自动写入 4 条样例申请：

| 单位 | 状态 | 说明 |
|------|------|------|
| 星河科技有限公司 | `REVIEW_APPROVED` | 经历完整的退回-修正-重新提交流程 |
| 远航物流有限公司 | `REVIEW_APPROVED` | 已复审通过，待复核/拨付 |
| 锦绣餐饮管理公司 | `REVIEW_REJECTED` | 复审未通过 |
| 绿源环保科技公司 | `DISBURSED` | 已拨付 |

可直接调用接口观察流转记录，例如：

```bash
curl http://localhost:8080/api/applications/1/flow-records | jq
```
