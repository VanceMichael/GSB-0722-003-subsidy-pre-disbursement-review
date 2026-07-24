package com.example.subsidy.controller;

import com.example.subsidy.dto.request.ApplicationCreateRequest;
import com.example.subsidy.dto.request.ApplicationStatusUpdateRequest;
import com.example.subsidy.dto.response.ApiResponse;
import com.example.subsidy.dto.response.ApplicationResponse;
import com.example.subsidy.dto.response.FlowRecordResponse;
import com.example.subsidy.service.SubsidyApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class SubsidyApplicationController {

    private final SubsidyApplicationService applicationService;

    public SubsidyApplicationController(SubsidyApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request) {
        return ApiResponse.success(applicationService.createApplication(request));
    }

    @GetMapping
    public ApiResponse<List<ApplicationResponse>> getAllApplications() {
        return ApiResponse.success(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getApplication(id));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<ApplicationResponse> approveReview(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApplicationStatusUpdateRequest request) {
        if (request == null) {
            request = new ApplicationStatusUpdateRequest();
        }
        return ApiResponse.success(applicationService.approveReview(id, request));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<ApplicationResponse> rejectReview(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApplicationStatusUpdateRequest request) {
        if (request == null) {
            request = new ApplicationStatusUpdateRequest();
        }
        return ApiResponse.success(applicationService.rejectReview(id, request));
    }

    @PutMapping("/{id}/correct")
    public ApiResponse<ApplicationResponse> submitCorrection(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApplicationStatusUpdateRequest request) {
        if (request == null) {
            request = new ApplicationStatusUpdateRequest();
        }
        return ApiResponse.success(applicationService.submitCorrection(id, request));
    }

    @PutMapping("/{id}/disburse")
    public ApiResponse<ApplicationResponse> disburse(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApplicationStatusUpdateRequest request) {
        if (request == null) {
            request = new ApplicationStatusUpdateRequest();
        }
        return ApiResponse.success(applicationService.disburse(id, request));
    }

    @GetMapping("/{id}/flow-records")
    public ApiResponse<List<FlowRecordResponse>> getFlowRecords(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getFlowRecords(id));
    }
}
