package com.gov.subsidy.review.service;

import com.gov.subsidy.review.domain.entity.FlowRecord;
import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.domain.enums.FlowAction;
import com.gov.subsidy.review.dto.ApplicationResponse;
import com.gov.subsidy.review.dto.CreateApplicationRequest;
import com.gov.subsidy.review.dto.DisburseRequest;
import com.gov.subsidy.review.dto.FlowRecordResponse;
import com.gov.subsidy.review.dto.ReReviewRequest;
import com.gov.subsidy.review.exception.BusinessRuleException;
import com.gov.subsidy.review.exception.ResourceNotFoundException;
import com.gov.subsidy.review.repository.FlowRecordRepository;
import com.gov.subsidy.review.repository.SubsidyApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请相关业务逻辑：登记、查询、流转记录读取。
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final SubsidyApplicationRepository applicationRepository;
    private final FlowRecordRepository flowRecordRepository;

    /** 登记新申请，初始状态为已登记，并写一笔流转记录。 */
    @Transactional
    public ApplicationResponse register(CreateApplicationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SubsidyApplication app = SubsidyApplication.builder()
                .employerName(request.employerName())
                .subsidyPeriod(request.subsidyPeriod())
                .appliedAmount(request.appliedAmount())
                .status(ApplicationStatus.REGISTERED)
                .createdAt(now)
                .updatedAt(now)
                .build();
        app = applicationRepository.save(app);

        flowRecordRepository.save(FlowRecord.builder()
                .applicationId(app.getId())
                .action(FlowAction.REGISTER)
                .fromStatus(null)
                .toStatus(ApplicationStatus.REGISTERED)
                .operator(request.employerName())
                .remark("登记补贴申请")
                .occurredAt(now)
                .build());

        return ApplicationResponse.from(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listAll() {
        return applicationRepository.findAll().stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getById(Long id) {
        return ApplicationResponse.from(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<FlowRecordResponse> getFlowRecords(Long applicationId) {
        getEntityOrThrow(applicationId);
        return flowRecordRepository.findByApplicationIdOrderByOccurredAtAsc(applicationId).stream()
                .map(FlowRecordResponse::from)
                .toList();
    }

    /**
     * 复审：将已登记或可修正的申请推进到复审通过/复审未通过。
     * 这是让新登记申请能够进入可发起复核状态的入口。
     */
    @Transactional
    public ApplicationResponse reReview(Long id, ReReviewRequest request) {
        SubsidyApplication app = getEntityOrThrow(id);
        if (!app.getStatus().canReReview()) {
            throw new BusinessRuleException(
                    "当前状态[" + app.getStatus().getLabel() + "]不允许复审，仅已登记或可修正的申请可进入复审");
        }

        LocalDateTime now = LocalDateTime.now();
        ApplicationStatus from = app.getStatus();
        ApplicationStatus to = request.passed()
                ? ApplicationStatus.RE_REVIEW_PASSED
                : ApplicationStatus.RE_REVIEW_REJECTED;
        FlowAction action = request.passed() ? FlowAction.RE_REVIEW_PASS : FlowAction.RE_REVIEW_REJECT;

        app.setStatus(to);
        app.setUpdatedAt(now);
        applicationRepository.save(app);

        recordFlow(id, null, action, from, to, request.operator(), request.remark(), now);
        return ApplicationResponse.from(app);
    }

    /** 拨付：将复核通过待拨付的申请推进到已拨付。 */
    @Transactional
    public ApplicationResponse disburse(Long id, DisburseRequest request) {
        SubsidyApplication app = getEntityOrThrow(id);
        if (!app.getStatus().canDisburse()) {
            throw new BusinessRuleException(
                    "当前状态[" + app.getStatus().getLabel() + "]不允许拨付，仅复核通过待拨付的申请可拨付");
        }

        LocalDateTime now = LocalDateTime.now();
        ApplicationStatus from = app.getStatus();
        app.setStatus(ApplicationStatus.DISBURSED);
        app.setUpdatedAt(now);
        applicationRepository.save(app);

        recordFlow(id, null, FlowAction.DISBURSE, from, ApplicationStatus.DISBURSED,
                request.operator(), request.remark(), now);
        return ApplicationResponse.from(app);
    }

    public SubsidyApplication getEntityOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请不存在: id=" + id));
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
