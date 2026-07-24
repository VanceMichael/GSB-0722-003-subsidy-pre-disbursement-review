package com.example.subsidy.controller;

import com.example.subsidy.common.ApiResponse;
import com.example.subsidy.dto.ApplicationCreateRequest;
import com.example.subsidy.dto.ApplicationDTO;
import com.example.subsidy.dto.FlowRecordDTO;
import com.example.subsidy.dto.OperatorRequest;
import com.example.subsidy.service.SubsidyApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class SubsidyApplicationController {

    private final SubsidyApplicationService applicationService;

    @PostMapping
    public ApiResponse<ApplicationDTO> register(@Valid @RequestBody ApplicationCreateRequest request) {
        return ApiResponse.ok(applicationService.register(request));
    }

    @GetMapping
    public ApiResponse<List<ApplicationDTO>> list() {
        return ApiResponse.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(applicationService.findById(id));
    }

    @GetMapping("/{id}/flow-records")
    public ApiResponse<List<FlowRecordDTO>> flowRecords(@PathVariable Long id) {
        return ApiResponse.ok(applicationService.getFlowRecords(id));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ApplicationDTO> submit(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.ok(applicationService.submitForReview(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ApplicationDTO> approve(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.ok(applicationService.approveReview(id, request));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ApplicationDTO> reject(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.ok(applicationService.rejectReview(id, request));
    }

    @PostMapping("/{id}/resubmit")
    public ApiResponse<ApplicationDTO> resubmit(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.ok(applicationService.resubmit(id, request));
    }

    @PostMapping("/{id}/disburse")
    public ApiResponse<ApplicationDTO> disburse(@PathVariable Long id, @Valid @RequestBody OperatorRequest request) {
        return ApiResponse.ok(applicationService.disburse(id, request));
    }
}
