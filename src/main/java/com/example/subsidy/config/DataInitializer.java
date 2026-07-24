package com.example.subsidy.config;

import com.example.subsidy.dto.ApplicationCreateRequest;
import com.example.subsidy.dto.OperatorRequest;
import com.example.subsidy.dto.ReviewCloseRequest;
import com.example.subsidy.dto.ReviewCreateRequest;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.RiskLevel;
import com.example.subsidy.service.PreDisbursementReviewService;
import com.example.subsidy.service.SubsidyApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SubsidyApplicationService applicationService;
    private final PreDisbursementReviewService reviewService;

    @Override
    public void run(String... args) {
        Long app1Id = createAndFlow("星河科技有限公司", "2024-Q1", new BigDecimal("50000.00"),
                "张三", "李四", "王五");

        Long app2Id = applicationService.register(ApplicationCreateRequest.builder()
                .unitName("远航物流有限公司")
                .subsidyPeriod("2024-Q2")
                .amount(new BigDecimal("32000.00"))
                .build()).getId();
        applicationService.submitForReview(app2Id, OperatorRequest.builder()
                .operator("张三").remark("提交复审").build());
        applicationService.approveReview(app2Id, OperatorRequest.builder()
                .operator("李四").remark("复审通过").build());

        Long app3Id = applicationService.register(ApplicationCreateRequest.builder()
                .unitName("锦绣餐饮管理公司")
                .subsidyPeriod("2024-Q1")
                .amount(new BigDecimal("18000.00"))
                .build()).getId();
        applicationService.submitForReview(app3Id, OperatorRequest.builder()
                .operator("张三").remark("提交复审").build());
        applicationService.rejectReview(app3Id, OperatorRequest.builder()
                .operator("李四").remark("材料不齐全").build());

        Long app4Id = applicationService.register(ApplicationCreateRequest.builder()
                .unitName("绿源环保科技公司")
                .subsidyPeriod("2024-Q3")
                .amount(new BigDecimal("75000.00"))
                .build()).getId();
        applicationService.submitForReview(app4Id, OperatorRequest.builder()
                .operator("张三").remark("提交复审").build());
        applicationService.approveReview(app4Id, OperatorRequest.builder()
                .operator("李四").remark("复审通过").build());
        applicationService.disburse(app4Id, OperatorRequest.builder()
                .operator("财务-赵六").remark("已拨付").build());
    }

    private Long createAndFlow(String unitName, String period, BigDecimal amount,
                               String submitter, String approver, String reviewer) {
        Long id = applicationService.register(ApplicationCreateRequest.builder()
                .unitName(unitName)
                .subsidyPeriod(period)
                .amount(amount)
                .build()).getId();
        applicationService.submitForReview(id, OperatorRequest.builder()
                .operator(submitter).remark("提交复审").build());
        applicationService.approveReview(id, OperatorRequest.builder()
                .operator(approver).remark("复审通过").build());

        Long reviewId = reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.MEDIUM)
                .problemDescription("部分员工社保缴纳月数不足，需核实")
                .processor(reviewer)
                .build()).getId();

        reviewService.closeReview(reviewId, ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.RETURN)
                .problemDescription("部分员工社保缴纳月数不足，需补充证明材料")
                .processor(reviewer)
                .build()).getId();

        applicationService.resubmit(id, OperatorRequest.builder()
                .operator(submitter).remark("已补充社保缴纳证明").build());

        return id;
    }
}
