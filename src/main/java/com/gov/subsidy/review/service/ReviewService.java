package com.gov.subsidy.review.service;

import com.gov.subsidy.review.domain.entity.FlowRecord;
import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.entity.SubsidyReview;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.FlowAction;
import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import com.gov.subsidy.review.domain.enums.ReviewStatus;
import com.gov.subsidy.review.dto.DecideReviewRequest;
import com.gov.subsidy.review.dto.ReviewResponse;
import com.gov.subsidy.review.dto.StartReviewRequest;
import com.gov.subsidy.review.exception.BusinessRuleException;
import com.gov.subsidy.review.exception.ResourceNotFoundException;
import com.gov.subsidy.review.repository.FlowRecordRepository;
import com.gov.subsidy.review.repository.SubsidyApplicationRepository;
import com.gov.subsidy.review.repository.SubsidyReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 复核相关业务逻辑：发起复核、做出结论（通过/退回）。
 * 业务约束：
 * 1. 只有"复审通过"（RE_REVIEW_PASSED）的申请可以发起复核；
 * 2. 已拨付、复审未通过的申请不能发起复核；
 * 3. 一个申请同时只能有一个未关闭（OPEN）的复核。
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final SubsidyReviewRepository reviewRepository;
    private final SubsidyApplicationRepository applicationRepository;
    private final FlowRecordRepository flowRecordRepository;
    private final ApplicationService applicationService;

    /** 发起复核。 */
    @Transactional
    public ReviewResponse startReview(Long applicationId, StartReviewRequest request) {
        SubsidyApplication app = applicationService.getEntityOrThrow(applicationId);

        if (!app.getStatus().canStartReview()) {
            throw new BusinessRuleException(
                    "当前状态[" + app.getStatus().getLabel() + "]不允许发起复核，仅复审通过且未拨付的申请可发起复核");
        }
        if (reviewRepository.existsByApplicationIdAndReviewStatus(applicationId, ReviewStatus.OPEN)) {
            throw new BusinessRuleException("该申请已存在一个未关闭的复核，不能重复发起");
        }

        LocalDateTime now = LocalDateTime.now();
        SubsidyReview review = SubsidyReview.builder()
                .applicationId(applicationId)
                .riskLevel(request.riskLevel())
                .problemDescription(request.problemDescription())
                .handler(request.handler())
                .reviewStatus(ReviewStatus.OPEN)
                .createdAt(now)
                .build();
        review = reviewRepository.save(review);

        ApplicationStatus from = app.getStatus();
        app.setStatus(ApplicationStatus.UNDER_REVIEW);
        app.setUpdatedAt(now);
        applicationRepository.save(app);

        recordFlow(applicationId, review.getId(), FlowAction.START_REVIEW,
                from, ApplicationStatus.UNDER_REVIEW, request.handler(),
                "发起复核，风险等级：" + request.riskLevel().getLabel(), now);

        return ReviewResponse.from(review);
    }

    /** 对未关闭的复核做出结论：通过或退回。 */
    @Transactional
    public ReviewResponse decide(Long reviewId, DecideReviewRequest request) {
        SubsidyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("复核不存在: id=" + reviewId));

        if (review.getReviewStatus() == ReviewStatus.CLOSED) {
            throw new BusinessRuleException("该复核已关闭，不能重复处理");
        }

        SubsidyApplication app = applicationService.getEntityOrThrow(review.getApplicationId());
        LocalDateTime now = LocalDateTime.now();

        review.setConclusion(request.conclusion());
        review.setHandler(request.handler());
        review.setReviewStatus(ReviewStatus.CLOSED);
        review.setClosedAt(now);
        reviewRepository.save(review);

        ApplicationStatus from = app.getStatus();
        ApplicationStatus to;
        FlowAction action;
        if (request.conclusion() == ReviewConclusion.APPROVED) {
            to = ApplicationStatus.APPROVED_FOR_DISBURSEMENT;
            action = FlowAction.REVIEW_APPROVED;
        } else {
            // 退回：申请进入可修正状态
            to = ApplicationStatus.CORRECTABLE;
            action = FlowAction.REVIEW_RETURNED;
        }
        app.setStatus(to);
        app.setUpdatedAt(now);
        applicationRepository.save(app);

        recordFlow(review.getApplicationId(), reviewId, action, from, to,
                request.handler(), request.remark(), now);

        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listByApplication(Long applicationId) {
        applicationService.getEntityOrThrow(applicationId);
        return reviewRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    private void recordFlow(Long applicationId, Long reviewId, FlowAction action,
                            ApplicationStatus from, ApplicationStatus to,
                            String operator, String remark, LocalDateTime now) {
        flowRecordRepository.save(FlowRecord.builder()
                .applicationId(applicationId)
                .reviewId(reviewId)
                .action(action)
                .fromStatus(from)
                .toStatus(to)
                .operator(operator)
                .remark(remark)
                .occurredAt(now)
                .build());
    }
}
