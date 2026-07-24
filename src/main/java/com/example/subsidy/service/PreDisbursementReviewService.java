package com.example.subsidy.service;

import com.example.subsidy.common.BusinessException;
import com.example.subsidy.dto.ReviewCloseRequest;
import com.example.subsidy.dto.ReviewCreateRequest;
import com.example.subsidy.dto.ReviewDTO;
import com.example.subsidy.entity.ApplicationFlowRecord;
import com.example.subsidy.entity.PreDisbursementReview;
import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.repository.ApplicationFlowRecordRepository;
import com.example.subsidy.repository.PreDisbursementReviewRepository;
import com.example.subsidy.repository.SubsidyApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreDisbursementReviewService {

    private final PreDisbursementReviewRepository reviewRepository;
    private final SubsidyApplicationRepository applicationRepository;
    private final ApplicationFlowRecordRepository flowRecordRepository;

    @Transactional
    public ReviewDTO initiateReview(Long applicationId, ReviewCreateRequest request) {
        SubsidyApplication app = getApplication(applicationId);

        if (reviewRepository.existsByApplicationAndStatus(app, ReviewStatus.OPEN)) {
            throw new BusinessException("该申请已存在未关闭的复核，不能重复发起");
        }
        if (app.getStatus() == ApplicationStatus.DISBURSED) {
            throw new BusinessException("申请已拨付，不能发起复核");
        }
        if (app.getStatus() == ApplicationStatus.REVIEW_REJECTED) {
            throw new BusinessException("申请复审未通过，不能发起复核");
        }
        if (app.getStatus() != ApplicationStatus.REVIEW_APPROVED) {
            throw new BusinessException(String.format(
                    "当前状态为[%s]，仅[复审通过]状态可发起拨付前复核",
                    app.getStatus().getDescription()));
        }

        LocalDateTime now = LocalDateTime.now();
        PreDisbursementReview review = PreDisbursementReview.builder()
                .application(app)
                .riskLevel(request.getRiskLevel())
                .problemDescription(request.getProblemDescription())
                .processor(request.getProcessor())
                .status(ReviewStatus.OPEN)
                .createdAt(now)
                .build();
        review = reviewRepository.save(review);

        ApplicationStatus from = app.getStatus();
        app.setStatus(ApplicationStatus.PRE_DISBURSEMENT_REVIEW);
        app.setUpdatedAt(now);
        applicationRepository.save(app);
        recordFlow(app, from, ApplicationStatus.PRE_DISBURSEMENT_REVIEW,
                "发起拨付前复核", request.getProblemDescription(), request.getProcessor());

        return ReviewDTO.from(review);
    }

    @Transactional
    public ReviewDTO closeReview(Long reviewId, ReviewCloseRequest request) {
        PreDisbursementReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("复核记录不存在，id=" + reviewId));

        if (review.getStatus() == ReviewStatus.CLOSED) {
            throw new BusinessException("该复核已关闭，不能重复关闭");
        }

        SubsidyApplication app = review.getApplication();
        LocalDateTime now = LocalDateTime.now();

        review.setConclusion(request.getConclusion());
        review.setProcessor(request.getProcessor());
        if (request.getProblemDescription() != null) {
            review.setProblemDescription(request.getProblemDescription());
        }
        review.setStatus(ReviewStatus.CLOSED);
        review.setClosedAt(now);
        review = reviewRepository.save(review);

        ApplicationStatus from = app.getStatus();
        ApplicationStatus to;
        String action;
        if (request.getConclusion() == ReviewConclusion.RETURN) {
            to = ApplicationStatus.RETURNED_FOR_CORRECTION;
            action = "复核退回";
        } else {
            to = ApplicationStatus.REVIEW_APPROVED;
            action = "复核通过";
        }
        app.setStatus(to);
        app.setUpdatedAt(now);
        applicationRepository.save(app);
        recordFlow(app, from, to, action, request.getProblemDescription(), request.getProcessor());

        return ReviewDTO.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> findByApplication(Long applicationId) {
        SubsidyApplication app = getApplication(applicationId);
        return reviewRepository.findByApplicationOrderByCreatedAtDesc(app).stream()
                .map(ReviewDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewDTO findById(Long id) {
        PreDisbursementReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("复核记录不存在，id=" + id));
        return ReviewDTO.from(review);
    }

    private void recordFlow(SubsidyApplication app, ApplicationStatus from, ApplicationStatus to,
                            String action, String remark, String operator) {
        ApplicationFlowRecord record = ApplicationFlowRecord.builder()
                .application(app)
                .fromStatus(from)
                .toStatus(to)
                .action(action)
                .remark(remark)
                .operator(operator)
                .createdAt(LocalDateTime.now())
                .build();
        flowRecordRepository.save(record);
    }

    private SubsidyApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("申请不存在，id=" + id));
    }
}
