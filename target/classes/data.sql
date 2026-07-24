-- 样例数据：就业补贴拨付前复核

-- 申请1：已登记（待复审）
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (1, '星辰科技有限公司', '2024年第一季度', 50000.00, 'REGISTERED', '2024-04-01 09:00:00', '2024-04-01 09:00:00');

-- 申请2：复审通过（可发起复核/拨付）
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (2, '华夏制造集团', '2024年第一季度', 120000.00, 'REVIEW_APPROVED', '2024-03-15 10:00:00', '2024-03-20 14:00:00');

-- 申请3：复审未通过
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (3, '盛达贸易公司', '2024年第一季度', 30000.00, 'REVIEW_REJECTED', '2024-03-10 11:00:00', '2024-03-18 16:00:00');

-- 申请4：已拨付
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (4, '远景新能源有限公司', '2024年第一季度', 80000.00, 'DISBURSED', '2024-02-20 08:30:00', '2024-03-05 10:00:00');

-- 申请5：复核中（存在未关闭的复核）
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (5, '瑞丰建筑工程公司', '2024年第二季度', 95000.00, 'PRE_REVIEWING', '2024-04-10 09:00:00', '2024-04-15 11:00:00');

-- 申请6：待修正（复核退回）
INSERT INTO subsidy_application (id, unit_name, subsidy_period, amount, status, created_at, updated_at)
VALUES (6, '创新医疗科技有限公司', '2024年第二季度', 65000.00, 'PENDING_CORRECTION', '2024-04-05 09:30:00', '2024-04-18 15:00:00');

-- 流转记录
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (1, 1, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-04-01 09:00:00');

INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (2, 2, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-03-15 10:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (3, 2, 'REGISTERED', 'REVIEW_APPROVED', '复审通过', '张审核', '材料齐全，符合条件', '2024-03-20 14:00:00');

INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (4, 3, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-03-10 11:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (5, 3, 'REGISTERED', 'REVIEW_REJECTED', '复审不通过', '李审核', '材料缺失，需补充社保缴纳证明', '2024-03-18 16:00:00');

INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (6, 4, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-02-20 08:30:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (7, 4, 'REGISTERED', 'REVIEW_APPROVED', '复审通过', '王审核', '审核通过', '2024-02-25 10:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (8, 4, 'REVIEW_APPROVED', 'DISBURSED', '拨付', '赵财务', '款项已拨付至指定账户', '2024-03-05 10:00:00');

INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (9, 5, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-04-10 09:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (10, 5, 'REGISTERED', 'REVIEW_APPROVED', '复审通过', '张审核', '审核通过', '2024-04-12 14:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (11, 5, 'REVIEW_APPROVED', 'PRE_REVIEWING', '发起复核', '陈复核', '发起拨付前复核', '2024-04-15 11:00:00');

INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (12, 6, NULL, 'REGISTERED', '登记申请', 'system', '新建申请登记', '2024-04-05 09:30:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (13, 6, 'REGISTERED', 'REVIEW_APPROVED', '复审通过', '李审核', '审核通过', '2024-04-08 10:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (14, 6, 'REVIEW_APPROVED', 'PRE_REVIEWING', '发起复核', '陈复核', '发起拨付前复核', '2024-04-15 14:00:00');
INSERT INTO application_flow_record (id, application_id, from_status, to_status, operation, operator, remark, operated_at)
VALUES (15, 6, 'PRE_REVIEWING', 'PENDING_CORRECTION', '复核退回', '陈复核', '退回修正，风险等级: 中风险，问题: 人员名单与社保缴纳记录不一致', '2024-04-18 15:00:00');

-- 复核记录：申请5的未关闭复核
INSERT INTO subsidy_review (id, application_id, status, initiated_by, initiated_at)
VALUES (1, 5, 'IN_PROGRESS', '陈复核', '2024-04-15 11:00:00');

-- 复核记录：申请6的已关闭复核（退回）
INSERT INTO subsidy_review (id, application_id, status, risk_level, problem_description, conclusion, handler, initiated_by, initiated_at, completed_at)
VALUES (2, 6, 'CLOSED', 'MEDIUM', '人员名单与社保缴纳记录不一致', 'RETURN', '陈复核', '陈复核', '2024-04-15 14:00:00', '2024-04-18 15:00:00');

-- 重置自增序列，避免与预插入ID冲突
ALTER TABLE subsidy_application ALTER COLUMN id RESTART WITH 100;
ALTER TABLE subsidy_review ALTER COLUMN id RESTART WITH 100;
ALTER TABLE application_flow_record ALTER COLUMN id RESTART WITH 100;
