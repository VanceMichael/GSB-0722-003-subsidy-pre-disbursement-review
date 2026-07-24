package com.gov.subsidy.review.service;

import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.entity.SubsidyReview;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import com.gov.subsidy.review.domain.enums.ReviewStatus;
import com.gov.subsidy.review.domain.enums.RiskLevel;
import com.gov.subsidy.review.dto.CreateApplicationRequest;
import com.gov.subsidy.review.dto.DecideReviewRequest;
import com.gov.subsidy.review.dto.DisburseRequest;
import com.gov.subsidy.review.dto.ReReviewRequest;
import com.gov.subsidy.review.dto.ReviewResponse;
import com.gov.subsidy.review.dto.StartReviewRequest;
import com.gov.subsidy.review.exception.BusinessRuleException;
import com.gov.subsidy.review.repository.FlowRecordRepository;
import com.gov.subsidy.review.repository.SubsidyApplicationRepository;
import com.gov.subsidy.review.repository.SubsidyReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 复核业务规则测试。
 */
@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private SubsidyApplicationRepository applicationRepository;
    @Autowired
    private SubsidyReviewRepository reviewRepository;
    @Autowired
    private FlowRecordRepository flowRecordRepository;

    @BeforeEach
    void clean() {
        flowRecordRepository.deleteAll();
        reviewRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    private Long newApplication(ApplicationStatus status) {
        LocalDateTime now = LocalDateTime.now();
        SubsidyApplication app = applicationRepository.save(SubsidyApplication.builder()
                .employerName("测试单位")
                .subsidyPeriod("2026-Q1")
                .appliedAmount(new BigDecimal("10000.00"))
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build());
        return app.getId();
    }

    private StartReviewRequest startRequest() {
        return new StartReviewRequest(RiskLevel.HIGH, "金额与人数不匹配", "复核员张三");
    }

    @Test
    void register_shouldCreateApplicationWithRegisteredStatusAndFlowRecord() {
        var resp = applicationService.register(
                new CreateApplicationRequest("阳光科技", "2026-Q1", new BigDecimal("120000.00")));

        assertThat(resp.status()).isEqualTo(ApplicationStatus.REGISTERED);
        assertThat(resp.appliedAmount()).isEqualByComparingTo("120000.00");
        assertThat(flowRecordRepository.findByApplicationIdOrderByOccurredAtAsc(resp.id())).hasSize(1);
    }

    @Test
    void startReview_shouldSucceed_whenApplicationReReviewPassed() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);

        ReviewResponse review = reviewService.startReview(appId, startRequest());

        assertThat(review.reviewStatus()).isEqualTo(ReviewStatus.OPEN);
        assertThat(applicationRepository.findById(appId).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    void startReview_shouldFail_whenApplicationDisbursed() {
        Long appId = newApplication(ApplicationStatus.DISBURSED);

        assertThatThrownBy(() -> reviewService.startReview(appId, startRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("不允许发起复核");
    }

    @Test
    void startReview_shouldFail_whenApplicationReReviewRejected() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_REJECTED);

        assertThatThrownBy(() -> reviewService.startReview(appId, startRequest()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void startReview_shouldFail_whenAnOpenReviewAlreadyExists() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        reviewService.startReview(appId, startRequest());
        // 申请此时已是复核中，且已有 OPEN 复核，重复发起应失败
        assertThatThrownBy(() -> reviewService.startReview(appId, startRequest()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void decideReturned_shouldMoveApplicationToCorrectableAndLeaveFullFlow() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        ReviewResponse review = reviewService.startReview(appId, startRequest());

        ReviewResponse decided = reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.RETURNED, "复核员李四", "材料不全，退回修正"));

        assertThat(decided.reviewStatus()).isEqualTo(ReviewStatus.CLOSED);
        assertThat(decided.conclusion()).isEqualTo(ReviewConclusion.RETURNED);
        assertThat(applicationRepository.findById(appId).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.CORRECTABLE);
        // 流转记录：发起复核 + 复核退回（登记记录本测试未写，因是直接造数据）
        assertThat(flowRecordRepository.findByApplicationIdOrderByOccurredAtAsc(appId)).hasSize(2);
    }

    @Test
    void decideApproved_shouldMoveApplicationToApprovedForDisbursement() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        ReviewResponse review = reviewService.startReview(appId, startRequest());

        reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.APPROVED, "复核员李四", "无异常，通过"));

        assertThat(applicationRepository.findById(appId).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.APPROVED_FOR_DISBURSEMENT);
    }

    @Test
    void decide_shouldFail_whenReviewAlreadyClosed() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        ReviewResponse review = reviewService.startReview(appId, startRequest());
        reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.APPROVED, "复核员李四", "通过"));

        assertThatThrownBy(() -> reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.RETURNED, "复核员李四", "重复处理")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("已关闭");
    }

    @Test
    void reReview_shouldMoveRegisteredToReReviewPassed() {
        var created = applicationService.register(
                new CreateApplicationRequest("阳光科技", "2026-Q1", new BigDecimal("120000.00")));

        var passed = applicationService.reReview(created.id(),
                new ReReviewRequest(true, "复审员赵六", "初审无误"));

        assertThat(passed.status()).isEqualTo(ApplicationStatus.RE_REVIEW_PASSED);
        // 之后即可发起复核
        ReviewResponse review = reviewService.startReview(created.id(), startRequest());
        assertThat(review.reviewStatus()).isEqualTo(ReviewStatus.OPEN);
    }

    @Test
    void reReview_shouldFail_whenStatusNotRegisterableOrCorrectable() {
        Long appId = newApplication(ApplicationStatus.DISBURSED);
        assertThatThrownBy(() -> applicationService.reReview(appId,
                new ReReviewRequest(true, "复审员赵六", "x")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void correctableApplication_canReEnterReReview() {
        // 退回后进入可修正，随后重新复审通过
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        ReviewResponse review = reviewService.startReview(appId, startRequest());
        reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.RETURNED, "复核员李四", "退回"));

        assertThat(applicationRepository.findById(appId).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.CORRECTABLE);

        var passed = applicationService.reReview(appId,
                new ReReviewRequest(true, "复审员赵六", "修正后通过"));
        assertThat(passed.status()).isEqualTo(ApplicationStatus.RE_REVIEW_PASSED);
    }

    @Test
    void disburse_shouldFail_unlessApprovedForDisbursement() {
        Long appId = newApplication(ApplicationStatus.RE_REVIEW_PASSED);
        assertThatThrownBy(() -> applicationService.disburse(appId,
                new DisburseRequest("出纳", "x")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void fullHappyPath_fromRegisterToDisbursed_leavesCompleteFlow() {
        var created = applicationService.register(
                new CreateApplicationRequest("阳光科技", "2026-Q1", new BigDecimal("120000.00")));
        Long appId = created.id();

        applicationService.reReview(appId, new ReReviewRequest(true, "复审员赵六", "初审无误"));
        ReviewResponse review = reviewService.startReview(appId, startRequest());
        reviewService.decide(review.id(),
                new DecideReviewRequest(ReviewConclusion.APPROVED, "复核员李四", "核查无异常"));
        var disbursed = applicationService.disburse(appId, new DisburseRequest("出纳王七", "已拨付"));

        assertThat(disbursed.status()).isEqualTo(ApplicationStatus.DISBURSED);
        // 流转记录：登记 + 复审通过 + 发起复核 + 复核通过 + 拨付 = 5
        assertThat(flowRecordRepository.findByApplicationIdOrderByOccurredAtAsc(appId)).hasSize(5);
    }
}
