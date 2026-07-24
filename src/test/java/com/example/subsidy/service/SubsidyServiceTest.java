package com.example.subsidy.service;

import com.example.subsidy.dto.request.*;
import com.example.subsidy.dto.response.ApplicationResponse;
import com.example.subsidy.dto.response.ReviewResponse;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.RiskLevel;
import com.example.subsidy.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SubsidyServiceTest {

    @Autowired
    private SubsidyApplicationService applicationService;

    @Autowired
    private SubsidyReviewService reviewService;

    private ApplicationCreateRequest buildCreateRequest(String unitName, BigDecimal amount) {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setUnitName(unitName);
        req.setSubsidyPeriod("2024年第一季度");
        req.setAmount(amount);
        return req;
    }

    @Test
    void createApplication_shouldSucceed() {
        ApplicationResponse resp = applicationService.createApplication(
                buildCreateRequest("测试公司A", new BigDecimal("50000.00")));
        assertNotNull(resp.getId());
        assertEquals("测试公司A", resp.getUnitName());
        assertEquals(0, new BigDecimal("50000.00").compareTo(resp.getAmount()));
        assertEquals(ApplicationStatus.REGISTERED, resp.getStatus());
    }

    @Test
    void approveReview_fromRegistered_shouldSucceed() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司B", new BigDecimal("30000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        ApplicationResponse approved = applicationService.approveReview(app.getId(), req);
        assertEquals(ApplicationStatus.REVIEW_APPROVED, approved.getStatus());
    }

    @Test
    void approveReview_fromNonRegistered_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司C", new BigDecimal("20000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> applicationService.approveReview(app.getId(), req));
        assertTrue(ex.getMessage().contains("只有已登记"));
    }

    @Test
    void initiateReview_whenApproved_shouldSucceed() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司D", new BigDecimal("80000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);
        assertNotNull(review.getId());
        assertEquals(app.getId(), review.getApplicationId());

        ApplicationResponse updated = applicationService.getApplication(app.getId());
        assertEquals(ApplicationStatus.PRE_REVIEWING, updated.getStatus());
    }

    @Test
    void initiateReview_whenDisbursed_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司E", new BigDecimal("60000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);
        req.setOperator("赵财务");
        applicationService.disburse(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.initiateReview(app.getId(), initiateReq));
        assertTrue(ex.getMessage().contains("已拨付"));
    }

    @Test
    void initiateReview_whenRejected_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司F", new BigDecimal("40000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("李审核");
        applicationService.rejectReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.initiateReview(app.getId(), initiateReq));
        assertTrue(ex.getMessage().contains("复审未通过"));
    }

    @Test
    void initiateReview_duplicateOpenReview_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司G", new BigDecimal("70000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        reviewService.initiateReview(app.getId(), initiateReq);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.initiateReview(app.getId(), initiateReq));
        assertTrue(ex.getMessage().contains("只有复审通过状态"));
    }

    @Test
    void completeReview_withReturn_shouldSetPendingCorrection() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司H", new BigDecimal("55000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);

        ReviewCompleteRequest completeReq = new ReviewCompleteRequest();
        completeReq.setRiskLevel(RiskLevel.MEDIUM);
        completeReq.setProblemDescription("人员名单与社保不一致");
        completeReq.setConclusion(ReviewConclusion.RETURN);
        completeReq.setHandler("陈复核");
        ReviewResponse completed = reviewService.completeReview(review.getId(), completeReq);

        assertEquals(ReviewConclusion.RETURN, completed.getConclusion());
        ApplicationResponse updated = applicationService.getApplication(app.getId());
        assertEquals(ApplicationStatus.PENDING_CORRECTION, updated.getStatus());
    }

    @Test
    void completeReview_withPass_shouldReturnToApproved() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司I", new BigDecimal("45000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);

        ReviewCompleteRequest completeReq = new ReviewCompleteRequest();
        completeReq.setRiskLevel(RiskLevel.LOW);
        completeReq.setConclusion(ReviewConclusion.PASS);
        completeReq.setHandler("陈复核");
        reviewService.completeReview(review.getId(), completeReq);

        ApplicationResponse updated = applicationService.getApplication(app.getId());
        assertEquals(ApplicationStatus.REVIEW_APPROVED, updated.getStatus());
    }

    @Test
    void submitCorrection_fromPendingCorrection_shouldReturnToApproved() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司J", new BigDecimal("35000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);

        ReviewCompleteRequest completeReq = new ReviewCompleteRequest();
        completeReq.setRiskLevel(RiskLevel.HIGH);
        completeReq.setProblemDescription("材料造假");
        completeReq.setConclusion(ReviewConclusion.RETURN);
        completeReq.setHandler("陈复核");
        reviewService.completeReview(review.getId(), completeReq);

        ApplicationStatusUpdateRequest correctReq = new ApplicationStatusUpdateRequest();
        correctReq.setOperator("测试公司J经办人");
        correctReq.setRemark("已补充真实材料");
        ApplicationResponse corrected = applicationService.submitCorrection(app.getId(), correctReq);
        assertEquals(ApplicationStatus.REVIEW_APPROVED, corrected.getStatus());
    }

    @Test
    void disburse_whenApprovedNoOpenReview_shouldSucceed() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司K", new BigDecimal("90000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);
        req.setOperator("赵财务");
        ApplicationResponse disbursed = applicationService.disburse(app.getId(), req);
        assertEquals(ApplicationStatus.DISBURSED, disbursed.getStatus());
    }

    @Test
    void disburse_withOpenReview_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司L", new BigDecimal("25000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        reviewService.initiateReview(app.getId(), initiateReq);

        req.setOperator("赵财务");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> applicationService.disburse(app.getId(), req));
        assertTrue(ex.getMessage().contains("只有复审通过状态"));
    }

    @Test
    void flowRecords_shouldBeComplete() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司M", new BigDecimal("15000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);

        ReviewCompleteRequest completeReq = new ReviewCompleteRequest();
        completeReq.setRiskLevel(RiskLevel.LOW);
        completeReq.setConclusion(ReviewConclusion.RETURN);
        completeReq.setHandler("陈复核");
        reviewService.completeReview(review.getId(), completeReq);

        req.setOperator("经办人");
        applicationService.submitCorrection(app.getId(), req);

        req.setOperator("赵财务");
        applicationService.disburse(app.getId(), req);

        var records = applicationService.getFlowRecords(app.getId());
        assertEquals(6, records.size());
        assertNull(records.get(0).getFromStatus());
        assertEquals(ApplicationStatus.REGISTERED, records.get(0).getToStatus());
        assertEquals("登记申请", records.get(0).getOperation());
        assertEquals(ApplicationStatus.DISBURSED, records.get(5).getToStatus());
    }

    @Test
    void completeReview_alreadyClosed_shouldFail() {
        ApplicationResponse app = applicationService.createApplication(
                buildCreateRequest("测试公司N", new BigDecimal("10000.00")));
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest();
        req.setOperator("张审核");
        applicationService.approveReview(app.getId(), req);

        ReviewInitiateRequest initiateReq = new ReviewInitiateRequest();
        initiateReq.setInitiatedBy("陈复核");
        ReviewResponse review = reviewService.initiateReview(app.getId(), initiateReq);

        ReviewCompleteRequest completeReq = new ReviewCompleteRequest();
        completeReq.setRiskLevel(RiskLevel.LOW);
        completeReq.setConclusion(ReviewConclusion.PASS);
        completeReq.setHandler("陈复核");
        reviewService.completeReview(review.getId(), completeReq);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.completeReview(review.getId(), completeReq));
        assertTrue(ex.getMessage().contains("已关闭"));
    }
}
