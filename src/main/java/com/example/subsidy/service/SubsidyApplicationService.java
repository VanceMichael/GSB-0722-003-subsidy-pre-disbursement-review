package com.example.subsidy.service;

import com.example.subsidy.dto.request.ApplicationCreateRequest;
import com.example.subsidy.dto.request.ApplicationStatusUpdateRequest;
import com.example.subsidy.dto.response.ApplicationResponse;
import com.example.subsidy.dto.response.FlowRecordResponse;
import com.example.subsidy.entity.ApplicationFlowRecord;
import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.exception.BusinessException;
import com.example.subsidy.repository.ApplicationFlowRecordRepository;
import com.example.subsidy.repository.SubsidyApplicationRepository;
import com.example.subsidy.repository.SubsidyReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubsidyApplicationService {

    private final SubsidyApplicationRepository applicationRepository;
    private final SubsidyReviewRepository reviewRepository;
    private final ApplicationFlowRecordRepository flowRecordRepository;

    public SubsidyApplicationService(SubsidyApplicationRepository applicationRepository,
                                     SubsidyReviewRepository reviewRepository,
                                     ApplicationFlowRecordRepository flowRecordRepository) {
        this.applicationRepository = applicationRepository;
        this.reviewRepository = reviewRepository;
        this.flowRecordRepository = flowRecordRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request) {
        SubsidyApplication application = new SubsidyApplication();
        application.setUnitName(request.getUnitName());
        application.setSubsidyPeriod(request.getSubsidyPeriod());
        application.setAmount(request.getAmount());
        application.setStatus(ApplicationStatus.REGISTERED);

        application = applicationRepository.save(application);

        addFlowRecord(application, null, ApplicationStatus.REGISTERED,
                "登记申请", "system", "新建申请登记");

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse approveReview(Long id, ApplicationStatusUpdateRequest request) {
        SubsidyApplication application = getApplicationOrThrow(id);

        if (application.getStatus() != ApplicationStatus.REGISTERED) {
            throw new BusinessException("只有已登记状态的申请才能通过复审");
        }

        ApplicationStatus fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.REVIEW_APPROVED);
        application = applicationRepository.save(application);

        addFlowRecord(application, fromStatus, ApplicationStatus.REVIEW_APPROVED,
                "复审通过", request.getOperator(), request.getRemark());

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse rejectReview(Long id, ApplicationStatusUpdateRequest request) {
        SubsidyApplication application = getApplicationOrThrow(id);

        if (application.getStatus() != ApplicationStatus.REGISTERED) {
            throw new BusinessException("只有已登记状态的申请才能做复审不通过操作");
        }

        ApplicationStatus fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.REVIEW_REJECTED);
        application = applicationRepository.save(application);

        addFlowRecord(application, fromStatus, ApplicationStatus.REVIEW_REJECTED,
                "复审不通过", request.getOperator(), request.getRemark());

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse submitCorrection(Long id, ApplicationStatusUpdateRequest request) {
        SubsidyApplication application = getApplicationOrThrow(id);

        if (application.getStatus() != ApplicationStatus.PENDING_CORRECTION) {
            throw new BusinessException("只有待修正状态的申请才能提交修正");
        }

        ApplicationStatus fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.REVIEW_APPROVED);
        application = applicationRepository.save(application);

        addFlowRecord(application, fromStatus, ApplicationStatus.REVIEW_APPROVED,
                "提交修正", request.getOperator(), request.getRemark());

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse disburse(Long id, ApplicationStatusUpdateRequest request) {
        SubsidyApplication application = getApplicationOrThrow(id);

        if (application.getStatus() != ApplicationStatus.REVIEW_APPROVED) {
            throw new BusinessException("只有复审通过状态的申请才能拨付");
        }

        boolean hasOpenReview = reviewRepository.existsByApplicationIdAndStatus(
                id, ReviewStatus.IN_PROGRESS);
        if (hasOpenReview) {
            throw new BusinessException("存在未关闭的复核，无法拨付");
        }

        ApplicationStatus fromStatus = application.getStatus();
        application.setStatus(ApplicationStatus.DISBURSED);
        application = applicationRepository.save(application);

        addFlowRecord(application, fromStatus, ApplicationStatus.DISBURSED,
                "拨付", request.getOperator(), request.getRemark());

        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id) {
        return toResponse(getApplicationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<FlowRecordResponse> getFlowRecords(Long id) {
        getApplicationOrThrow(id);
        return flowRecordRepository.findByApplicationIdOrderByOperatedAtAsc(id).stream()
                .map(this::toFlowRecordResponse)
                .collect(Collectors.toList());
    }

    void addFlowRecord(SubsidyApplication application, ApplicationStatus fromStatus,
                       ApplicationStatus toStatus, String operation, String operator, String remark) {
        ApplicationFlowRecord record = new ApplicationFlowRecord();
        record.setApplication(application);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setOperation(operation);
        record.setOperator(operator);
        record.setRemark(remark);
        flowRecordRepository.save(record);
        application.getFlowRecords().add(record);
    }

    SubsidyApplication getApplicationOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "申请不存在，ID: " + id));
    }

    ApplicationResponse toResponse(SubsidyApplication application) {
        return ApplicationResponse.of(
                application.getId(),
                application.getUnitName(),
                application.getSubsidyPeriod(),
                application.getAmount(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    private FlowRecordResponse toFlowRecordResponse(ApplicationFlowRecord record) {
        FlowRecordResponse resp = new FlowRecordResponse();
        resp.setId(record.getId());
        resp.setFromStatus(record.getFromStatus());
        resp.setFromStatusDescription(record.getFromStatus() != null ? record.getFromStatus().getDescription() : null);
        resp.setToStatus(record.getToStatus());
        resp.setToStatusDescription(record.getToStatus().getDescription());
        resp.setOperation(record.getOperation());
        resp.setOperator(record.getOperator());
        resp.setRemark(record.getRemark());
        resp.setOperatedAt(record.getOperatedAt());
        return resp;
    }
}
