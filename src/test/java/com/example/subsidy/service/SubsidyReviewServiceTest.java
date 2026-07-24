package com.example.subsidy.service;

import com.example.subsidy.common.BusinessException;
import com.example.subsidy.dto.ApplicationCreateRequest;
import com.example.subsidy.dto.ApplicationDTO;
import com.example.subsidy.dto.OperatorRequest;
import com.example.subsidy.dto.ReviewCloseRequest;
import com.example.subsidy.dto.ReviewCreateRequest;
import com.example.subsidy.dto.ReviewDTO;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.enums.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class SubsidyReviewServiceTest {

    @Autowired
    private SubsidyApplicationService applicationService;

    @Autowired
    private PreDisbursementReviewService reviewService;

    private ApplicationCreateRequest sampleRequest(String unitName) {
        return ApplicationCreateRequest.builder()
                .unitName(unitName)
                .subsidyPeriod("2024-Q1")
                .amount(new BigDecimal("50000.00"))
                .build();
    }

    private OperatorRequest op(String name) {
        return OperatorRequest.builder().operator(name).remark("test").build();
    }

    private Long createApprovedApplication(String unitName) {
        Long id = applicationService.register(sampleRequest(unitName)).getId();
        applicationService.submitForReview(id, op("提交人"));
        applicationService.approveReview(id, op("复审人"));
        return id;
    }

    @Test
    void register_shouldCreateApplicationWithRegisteredStatus() {
        ApplicationDTO dto = applicationService.register(sampleRequest("测试单位A"));
        assertNotNull(dto.getId());
        assertEquals("测试单位A", dto.getUnitName());
        assertEquals(0, new BigDecimal("50000.00").compareTo(dto.getAmount()));
        assertEquals(ApplicationStatus.REGISTERED, dto.getStatus());
    }

    @Test
    void fullHappyPath_shouldPassThroughAllStatesToDisbursed() {
        Long id = createApprovedApplication("测试单位B");

        ReviewDTO review = reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.LOW)
                .problemDescription("无异常")
                .processor("复核人")
                .build());
        assertNotNull(review.getId());
        assertEquals(ReviewStatus.OPEN, review.getStatus());
        assertEquals(ApplicationStatus.PRE_DISBURSEMENT_REVIEW, applicationService.findById(id).getStatus());

        reviewService.closeReview(review.getId(), ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.PASS)
                .processor("复核人")
                .build());
        assertEquals(ApplicationStatus.REVIEW_APPROVED, applicationService.findById(id).getStatus());

        applicationService.disburse(id, op("财务"));
        assertEquals(ApplicationStatus.DISBURSED, applicationService.findById(id).getStatus());
    }

    @Test
    void returnFlow_shouldAllowCorrectionAndResubmission() {
        Long id = createApprovedApplication("测试单位C");

        Long reviewId = reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.HIGH)
                .problemDescription("社保月数不足")
                .processor("复核人")
                .build()).getId();

        reviewService.closeReview(reviewId, ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.RETURN)
                .problemDescription("需补充社保证明")
                .processor("复核人")
                .build());
        assertEquals(ApplicationStatus.RETURNED_FOR_CORRECTION, applicationService.findById(id).getStatus());

        applicationService.resubmit(id, op("提交人"));
        assertEquals(ApplicationStatus.REVIEW_APPROVED, applicationService.findById(id).getStatus());

        applicationService.disburse(id, op("财务"));
        assertEquals(ApplicationStatus.DISBURSED, applicationService.findById(id).getStatus());
    }

    @Test
    void initiateReview_shouldFailWhenDisbursed() {
        Long id = createApprovedApplication("测试单位D");
        applicationService.disburse(id, op("财务"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.initiateReview(id, ReviewCreateRequest.builder()
                        .riskLevel(RiskLevel.LOW)
                        .processor("复核人")
                        .build()));
        assertTrue(ex.getMessage().contains("已拨付"));
    }

    @Test
    void initiateReview_shouldFailWhenRejected() {
        Long id = applicationService.register(sampleRequest("测试单位E")).getId();
        applicationService.submitForReview(id, op("提交人"));
        applicationService.rejectReview(id, op("复审人"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.initiateReview(id, ReviewCreateRequest.builder()
                        .riskLevel(RiskLevel.LOW)
                        .processor("复核人")
                        .build()));
        assertTrue(ex.getMessage().contains("复审未通过"));
    }

    @Test
    void initiateReview_shouldFailWhenNotApproved() {
        Long id = applicationService.register(sampleRequest("测试单位F")).getId();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.initiateReview(id, ReviewCreateRequest.builder()
                        .riskLevel(RiskLevel.LOW)
                        .processor("复核人")
                        .build()));
        assertTrue(ex.getMessage().contains("复审通过"));
    }

    @Test
    void initiateReview_shouldFailWhenOpenReviewExists() {
        Long id = createApprovedApplication("测试单位G");
        reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.MEDIUM)
                .processor("复核人")
                .build());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.initiateReview(id, ReviewCreateRequest.builder()
                        .riskLevel(RiskLevel.LOW)
                        .processor("复核人2")
                        .build()));
        assertTrue(ex.getMessage().contains("未关闭的复核"));
    }

    @Test
    void closeReview_shouldFailWhenAlreadyClosed() {
        Long id = createApprovedApplication("测试单位H");
        Long reviewId = reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.LOW)
                .processor("复核人")
                .build()).getId();
        reviewService.closeReview(reviewId, ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.PASS)
                .processor("复核人")
                .build());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.closeReview(reviewId, ReviewCloseRequest.builder()
                        .conclusion(ReviewConclusion.RETURN)
                        .processor("复核人")
                        .build()));
        assertTrue(ex.getMessage().contains("已关闭"));
    }

    @Test
    void invalidTransition_shouldFail() {
        Long id = applicationService.register(sampleRequest("测试单位I")).getId();

        assertThrows(BusinessException.class, () -> applicationService.approveReview(id, op("复审人")));
        assertThrows(BusinessException.class, () -> applicationService.disburse(id, op("财务")));
    }

    @Test
    void flowRecords_shouldBeRecordedForEveryTransition() {
        Long id = createApprovedApplication("测试单位J");
        assertEquals(3, applicationService.getFlowRecords(id).size());

        Long reviewId = reviewService.initiateReview(id, ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.MEDIUM)
                .processor("复核人")
                .build()).getId();
        assertEquals(4, applicationService.getFlowRecords(id).size());

        reviewService.closeReview(reviewId, ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.RETURN)
                .processor("复核人")
                .build());
        assertEquals(5, applicationService.getFlowRecords(id).size());
    }
}
