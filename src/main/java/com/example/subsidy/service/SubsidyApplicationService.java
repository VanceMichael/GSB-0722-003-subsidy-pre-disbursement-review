package com.example.subsidy.service;

import com.example.subsidy.common.BusinessException;
import com.example.subsidy.dto.ApplicationCreateRequest;
import com.example.subsidy.dto.ApplicationDTO;
import com.example.subsidy.dto.FlowRecordDTO;
import com.example.subsidy.dto.OperatorRequest;
import com.example.subsidy.entity.ApplicationFlowRecord;
import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.repository.ApplicationFlowRecordRepository;
import com.example.subsidy.repository.SubsidyApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubsidyApplicationService {

    private final SubsidyApplicationRepository applicationRepository;
    private final ApplicationFlowRecordRepository flowRecordRepository;

    @Transactional
    public ApplicationDTO register(ApplicationCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SubsidyApplication app = SubsidyApplication.builder()
                .unitName(request.getUnitName())
                .subsidyPeriod(request.getSubsidyPeriod())
                .amount(request.getAmount())
                .status(ApplicationStatus.REGISTERED)
                .createdAt(now)
                .updatedAt(now)
                .build();
        app = applicationRepository.save(app);
        recordFlow(app, null, ApplicationStatus.REGISTERED, "登记申请", "申请已登记", null);
        return ApplicationDTO.from(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDTO> findAll() {
        return applicationRepository.findAll().stream()
                .map(ApplicationDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDTO findById(Long id) {
        return ApplicationDTO.from(getApplication(id));
    }

    @Transactional(readOnly = true)
    public List<FlowRecordDTO> getFlowRecords(Long id) {
        SubsidyApplication app = getApplication(id);
        return flowRecordRepository.findByApplicationOrderByCreatedAtAsc(app).stream()
                .map(FlowRecordDTO::from)
                .toList();
    }

    @Transactional
    public ApplicationDTO submitForReview(Long id, OperatorRequest request) {
        SubsidyApplication app = getApplication(id);
        assertStatus(app, ApplicationStatus.REGISTERED, "提交复审");
        return transition(app, ApplicationStatus.UNDER_REVIEW, "提交复审", request);
    }

    @Transactional
    public ApplicationDTO approveReview(Long id, OperatorRequest request) {
        SubsidyApplication app = getApplication(id);
        assertStatus(app, ApplicationStatus.UNDER_REVIEW, "复审通过");
        return transition(app, ApplicationStatus.REVIEW_APPROVED, "复审通过", request);
    }

    @Transactional
    public ApplicationDTO rejectReview(Long id, OperatorRequest request) {
        SubsidyApplication app = getApplication(id);
        assertStatus(app, ApplicationStatus.UNDER_REVIEW, "复审不通过");
        return transition(app, ApplicationStatus.REVIEW_REJECTED, "复审不通过", request);
    }

    @Transactional
    public ApplicationDTO resubmit(Long id, OperatorRequest request) {
        SubsidyApplication app = getApplication(id);
        assertStatus(app, ApplicationStatus.RETURNED_FOR_CORRECTION, "修正后重新提交");
        return transition(app, ApplicationStatus.REVIEW_APPROVED, "修正后重新提交", request);
    }

    @Transactional
    public ApplicationDTO disburse(Long id, OperatorRequest request) {
        SubsidyApplication app = getApplication(id);
        assertStatus(app, ApplicationStatus.REVIEW_APPROVED, "拨付");
        return transition(app, ApplicationStatus.DISBURSED, "拨付", request);
    }

    private ApplicationDTO transition(SubsidyApplication app, ApplicationStatus to, String action, OperatorRequest request) {
        ApplicationStatus from = app.getStatus();
        app.setStatus(to);
        app.setUpdatedAt(LocalDateTime.now());
        app = applicationRepository.save(app);
        recordFlow(app, from, to, action, request.getRemark(), request.getOperator());
        return ApplicationDTO.from(app);
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

    private void assertStatus(SubsidyApplication app, ApplicationStatus expected, String action) {
        if (app.getStatus() != expected) {
            throw new BusinessException(String.format(
                    "当前状态为[%s]，无法执行[%s]操作，要求状态为[%s]",
                    app.getStatus().getDescription(), action, expected.getDescription()));
        }
    }

    SubsidyApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("申请不存在，id=" + id));
    }
}
