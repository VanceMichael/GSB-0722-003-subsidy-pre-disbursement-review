package com.example.subsidy.service;

import com.example.subsidy.dto.request.ReviewCompleteRequest;
import com.example.subsidy.dto.request.ReviewInitiateRequest;
import com.example.subsidy.dto.response.ReviewResponse;
import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.entity.SubsidyReview;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.exception.BusinessException;
import com.example.subsidy.repository.SubsidyReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubsidyReviewService {

    private final SubsidyReviewRepository reviewRepository;
    private final SubsidyApplicationService applicationService;

    public SubsidyReviewService(SubsidyReviewRepository reviewRepository,
                                SubsidyApplicationService applicationService) {
        this.reviewRepository = reviewRepository;
        this.applicationService = applicationService;
    }

    @Transactional
    public ReviewResponse initiateReview(Long applicationId, ReviewInitiateRequest request) {
        SubsidyApplication application = applicationService.getApplicationOrThrow(applicationId);

        if (application.getStatus() == ApplicationStatus.DISBURSED) {
            throw new BusinessException("已拨付的申请不能发起复核");
        }
        if (application.getStatus() == ApplicationStatus.REVIEW_REJECTED) {
            throw new BusinessException("复审未通过的申请不能发起复核");
        }
        if (application.getStatus() != ApplicationStatus.REVIEW_APPROVED) {
            throw new BusinessException("只有复审通过状态的申请才能发起复核，当前状态: "
                    + application.getStatus().getDescription());
        }

        boolean hasOpenReview = reviewRepository.existsByApplicationIdAndStatus(
                applicationId, ReviewStatus.IN_PROGRESS);
        if (hasOpenReview) {
            throw new BusinessException("该申请已存在未关闭的复核，不能重复发起");
        }

        ApplicationStatus fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.PRE_REVIEWING);

        SubsidyReview review = new SubsidyReview();
        review.setApplication(application);
        review.setStatus(ReviewStatus.IN_PROGRESS);
        review.setInitiatedBy(request.getInitiatedBy());

        review = reviewRepository.save(review);

        applicationService.addFlowRecord(application, fromStatus, ApplicationStatus.PRE_REVIEWING,
                "发起复核", request.getInitiatedBy(), "发起拨付前复核");

        return toResponse(review);
    }

    @Transactional
    public ReviewResponse completeReview(Long reviewId, ReviewCompleteRequest request) {
        SubsidyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "复核记录不存在，ID: " + reviewId));

        if (review.getStatus() == ReviewStatus.CLOSED) {
            throw new BusinessException("该复核已关闭，不能重复处理");
        }

        SubsidyApplication application = review.getApplication();
        ApplicationStatus fromStatus = application.getStatus();

        review.setRiskLevel(request.getRiskLevel());
        review.setProblemDescription(request.getProblemDescription());
        review.setConclusion(request.getConclusion());
        review.setHandler(request.getHandler());
        review.setStatus(ReviewStatus.CLOSED);
        review.setCompletedAt(LocalDateTime.now());

        ApplicationStatus toStatus;
        String operation;
        String remark;

        if (request.getConclusion() == ReviewConclusion.PASS) {
            toStatus = ApplicationStatus.REVIEW_APPROVED;
            operation = "复核通过";
            remark = "风险等级: " + request.getRiskLevel().getDescription();
        } else {
            toStatus = ApplicationStatus.PENDING_CORRECTION;
            operation = "复核退回";
            remark = "退回修正，风险等级: " + request.getRiskLevel().getDescription();
        }

        application.setStatus(toStatus);
        review = reviewRepository.save(review);

        String fullRemark = remark;
        if (request.getProblemDescription() != null && !request.getProblemDescription().isEmpty()) {
            fullRemark = remark + "，问题: " + request.getProblemDescription();
        }

        applicationService.addFlowRecord(application, fromStatus, toStatus,
                operation, request.getHandler(), fullRemark);

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByApplication(Long applicationId) {
        applicationService.getApplicationOrThrow(applicationId);
        return reviewRepository.findByApplicationIdOrderByInitiatedAtDesc(applicationId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        SubsidyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(404, "复核记录不存在，ID: " + reviewId));
        return toResponse(review);
    }

    private ReviewResponse toResponse(SubsidyReview review) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(review.getId());
        resp.setApplicationId(review.getApplication().getId());
        resp.setStatus(review.getStatus());
        resp.setStatusDescription(review.getStatus().getDescription());
        resp.setRiskLevel(review.getRiskLevel());
        resp.setRiskLevelDescription(review.getRiskLevel() != null ? review.getRiskLevel().getDescription() : null);
        resp.setProblemDescription(review.getProblemDescription());
        resp.setConclusion(review.getConclusion());
        resp.setConclusionDescription(review.getConclusion() != null ? review.getConclusion().getDescription() : null);
        resp.setHandler(review.getHandler());
        resp.setInitiatedBy(review.getInitiatedBy());
        resp.setInitiatedAt(review.getInitiatedAt());
        resp.setCompletedAt(review.getCompletedAt());
        return resp;
    }
}
