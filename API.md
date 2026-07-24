# 就业补贴拨付前复核服务 API 文档

## 项目概述

就业补贴拨付前复核服务，提供补贴申请登记、复审、拨付前复核、退回修正及拨付全流程管理。

### 技术栈

- Java 17 + Spring Boot 3.3.5
- Spring Data JPA + H2 内存数据库
- Spring Validation 参数校验
- Maven 构建

### 数据模型

**申请状态（ApplicationStatus）**

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `REGISTERED` | 已登记 | 初始状态，等待复审 |
| `REVIEW_APPROVED` | 复审通过 | 复审通过，可发起拨付前复核或拨付 |
| `REVIEW_REJECTED` | 复审未通过 | 复审不通过，不可发起复核 |
| `PRE_REVIEWING` | 复核中 | 拨付前复核进行中 |
| `PENDING_CORRECTION` | 待修正 | 复核退回，需修正后重新提交 |
| `DISBURSED` | 已拨付 | 款项已拨付，终态 |

**复核状态（ReviewStatus）**：`IN_PROGRESS`（进行中）/ `CLOSED`（已关闭）

**风险等级（RiskLevel）**：`LOW`（低风险）/ `MEDIUM`（中风险）/ `HIGH`（高风险）

**处理结论（ReviewConclusion）**：`PASS`（复核通过）/ `RETURN`（退回修正）

### 状态流转图

```
REGISTERED ──复审通过──▶ REVIEW_APPROVED ──发起复核──▶ PRE_REVIEWING
     │                       │  ▲                    │          │
     │                       │  │                    │          │
  复审不通过                 拨付  └──复核通过──┘     复核退回     │
     │                       │                            │          │
     ▼                       │                            ▼          │
REVIEW_REJECTED              │                    PENDING_CORRECTION
     │                       │                            │
     │                       └──────提交修正──────────────┘
     ▼
DISBURSED (终态)
```

---

## 接口列表

基础路径：`http://localhost:8080/api`

所有响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

### 1. 登记申请

**POST** `/api/applications`

- **状态前提**：无（新建接口）
- **请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| unitName | String | 是 | 单位名称，最长200字符 |
| subsidyPeriod | String | 是 | 补贴周期，最长50字符 |
| amount | BigDecimal | 是 | 申请金额，大于0，最多13位整数2位小数 |

```json
{
  "unitName": "示例科技有限公司",
  "subsidyPeriod": "2024年第三季度",
  "amount": 50000.00
}
```

- **响应**：返回创建的申请信息，状态为 `REGISTERED`
- **HTTP 状态码**：201 Created

---

### 2. 查询申请列表

**GET** `/api/applications`

- **状态前提**：无
- **响应**：返回所有申请，按创建时间倒序

---

### 3. 查询单个申请

**GET** `/api/applications/{id}`

- **状态前提**：无
- **路径参数**：`id` - 申请ID
- **响应**：返回申请详情
- **错误**：404 - 申请不存在

---

### 4. 复审通过

**PUT** `/api/applications/{id}/approve`

- **状态前提**：申请状态必须为 `REGISTERED`（已登记）
- **路径参数**：`id` - 申请ID
- **请求体**（可选）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| operator | String | 否 | 操作人 |
| remark | String | 否 | 备注 |

- **响应**：返回更新后的申请信息，状态变为 `REVIEW_APPROVED`
- **错误**：400 - 当前状态不允许此操作

---

### 5. 复审不通过

**PUT** `/api/applications/{id}/reject`

- **状态前提**：申请状态必须为 `REGISTERED`（已登记）
- **路径参数**：`id` - 申请ID
- **请求体**（可选）：operator、remark
- **响应**：返回更新后的申请信息，状态变为 `REVIEW_REJECTED`
- **错误**：400 - 当前状态不允许此操作

---

### 6. 发起拨付前复核

**POST** `/api/reviews/application/{applicationId}`

- **状态前提**：
  - 申请状态必须为 `REVIEW_APPROVED`（复审通过）
  - 申请不能为 `DISBURSED`（已拨付）或 `REVIEW_REJECTED`（复审未通过）
  - 该申请不能存在未关闭的复核
- **路径参数**：`applicationId` - 申请ID
- **请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| initiatedBy | String | 是 | 复核发起人，最长100字符 |

```json
{
  "initiatedBy": "陈复核"
}
```

- **响应**：返回创建的复核记录，状态为 `IN_PROGRESS`；申请状态变为 `PRE_REVIEWING`
- **HTTP 状态码**：201 Created
- **错误**：
  - 400 - 已拨付的申请不能发起复核
  - 400 - 复审未通过的申请不能发起复核
  - 400 - 只有复审通过状态的申请才能发起复核
  - 400 - 已存在未关闭的复核

---

### 7. 完成复核

**PUT** `/api/reviews/{reviewId}/complete`

- **状态前提**：复核记录状态必须为 `IN_PROGRESS`（进行中）
- **路径参数**：`reviewId` - 复核记录ID
- **请求体**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| riskLevel | String | 是 | 风险等级：LOW / MEDIUM / HIGH |
| problemDescription | String | 否 | 问题描述，最长1000字符 |
| conclusion | String | 是 | 处理结论：PASS（通过）/ RETURN（退回修正） |
| handler | String | 是 | 处理人，最长100字符 |

```json
{
  "riskLevel": "MEDIUM",
  "problemDescription": "人员名单与社保不一致",
  "conclusion": "RETURN",
  "handler": "陈复核"
}
```

- **响应**：返回更新后的复核记录，状态为 `CLOSED`
  - 结论为 `PASS`：申请状态回到 `REVIEW_APPROVED`，可拨付
  - 结论为 `RETURN`：申请状态变为 `PENDING_CORRECTION`，需修正
- **错误**：
  - 404 - 复核记录不存在
  - 400 - 该复核已关闭，不能重复处理

---

### 8. 查询复核记录

**GET** `/api/reviews/{reviewId}`

- **状态前提**：无
- **路径参数**：`reviewId` - 复核记录ID
- **响应**：返回复核详情

---

### 9. 查询申请的所有复核记录

**GET** `/api/reviews/application/{applicationId}`

- **状态前提**：无
- **路径参数**：`applicationId` - 申请ID
- **响应**：返回该申请的所有复核记录，按发起时间倒序

---

### 10. 提交修正

**PUT** `/api/applications/{id}/correct`

- **状态前提**：申请状态必须为 `PENDING_CORRECTION`（待修正）
- **路径参数**：`id` - 申请ID
- **请求体**（可选）：operator、remark
- **响应**：返回更新后的申请信息，状态回到 `REVIEW_APPROVED`
- **错误**：400 - 只有待修正状态的申请才能提交修正

---

### 11. 拨付

**PUT** `/api/applications/{id}/disburse`

- **状态前提**：
  - 申请状态必须为 `REVIEW_APPROVED`（复审通过）
  - 不存在未关闭的复核记录
- **路径参数**：`id` - 申请ID
- **请求体**（可选）：operator、remark
- **响应**：返回更新后的申请信息，状态变为 `DISBURSED`（终态）
- **错误**：
  - 400 - 只有复审通过状态的申请才能拨付
  - 400 - 存在未关闭的复核，无法拨付

---

### 12. 查询流转记录

**GET** `/api/applications/{id}/flow-records`

- **状态前提**：无
- **路径参数**：`id` - 申请ID
- **响应**：返回该申请的完整流转记录，按操作时间正序。每条记录包含：
  - fromStatus / toStatus：状态变更前后
  - operation：操作类型
  - operator：操作人
  - remark：备注
  - operatedAt：操作时间

---

## 运行命令

### 环境要求

- JDK 17+
- Maven 3.8+

### 运行服务

```bash
# 编译项目
mvn clean package -DskipTests

# 启动服务
mvn spring-boot:run

# 或运行打包后的 JAR
java -jar target/subsidy-review-1.0.0.jar
```

服务启动后监听 `http://localhost:8080`。

### 运行测试

```bash
# 运行所有测试（23个测试用例）
mvn clean test

# 仅运行 Service 层测试
mvn test -Dtest=SubsidyServiceTest

# 仅运行 Controller 层测试
mvn test -Dtest=SubsidyControllerTest
```

### H2 控制台

启动后可通过浏览器访问 H2 数据库控制台：

- 地址：`http://localhost:8080/h2-console`
- JDBC URL：`jdbc:h2:mem:subsidy`
- 用户名：`sa`
- 密码：（空）

### 样例数据

服务启动时自动初始化 6 条样例申请数据，覆盖所有状态：

| ID | 单位名称 | 状态 | 金额 |
|----|----------|------|------|
| 1 | 星辰科技有限公司 | REGISTERED | 50,000 |
| 2 | 华夏制造集团 | REVIEW_APPROVED | 120,000 |
| 3 | 盛达贸易公司 | REVIEW_REJECTED | 30,000 |
| 4 | 远景新能源有限公司 | DISBURSED | 80,000 |
| 5 | 瑞丰建筑工程公司 | PRE_REVIEWING | 95,000 |
| 6 | 创新医疗科技有限公司 | PENDING_CORRECTION | 65,000 |

---

## 项目结构

```
src/main/java/com/example/subsidy/
├── SubsidyReviewApplication.java      # Spring Boot 启动类
├── entity/                            # JPA 实体
│   ├── SubsidyApplication.java        # 补贴申请
│   ├── SubsidyReview.java             # 拨付前复核
│   └── ApplicationFlowRecord.java     # 流转记录
├── enums/                             # 枚举
│   ├── ApplicationStatus.java
│   ├── ReviewStatus.java
│   ├── RiskLevel.java
│   └── ReviewConclusion.java
├── repository/                        # Spring Data JPA Repository
├── dto/
│   ├── request/                       # 请求 DTO（含 Validation 注解）
│   └── response/                      # 响应 DTO
├── service/                           # 业务逻辑层
│   ├── SubsidyApplicationService.java
│   └── SubsidyReviewService.java
├── controller/                        # REST 控制器
│   ├── SubsidyApplicationController.java
│   └── SubsidyReviewController.java
└── exception/                         # 异常处理
    ├── BusinessException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml                    # 应用配置
└── data.sql                           # 样例数据初始化

src/test/java/com/example/subsidy/
├── service/SubsidyServiceTest.java    # Service 层单元测试（14个）
└── controller/SubsidyControllerTest.java  # Controller 集成测试（9个）
```
