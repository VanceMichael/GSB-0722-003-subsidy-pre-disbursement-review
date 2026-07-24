package com.gov.subsidy.review.controller;

import com.gov.subsidy.review.dto.ApplicationResponse;
import com.gov.subsidy.review.dto.CreateApplicationRequest;
import com.gov.subsidy.review.dto.DisburseRequest;
import com.gov.subsidy.review.dto.FlowRecordResponse;
import com.gov.subsidy.review.dto.ReReviewRequest;
import com.gov.subsidy.review.dto.ReviewResponse;
import com.gov.subsidy.review.service.ApplicationService;
import com.gov.subsidy.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 申请相关接口。控制器只负责编排，不含持久化逻辑。 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ReviewService reviewService;

    /** 登记申请。 */
    @PostMapping
    public ResponseEntity<ApplicationResponse> register(@Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.register(request));
    }

    /** 查询全部申请。 */
    @GetMapping
    public List<ApplicationResponse> listAll() {
        return applicationService.listAll();
    }

    /** 查询单个申请。 */
    @GetMapping("/{id}")
    public ApplicationResponse getById(@PathVariable Long id) {
        return applicationService.getById(id);
    }

    /** 复审：推进到复审通过 / 复审未通过。 */
    @PostMapping("/{id}/re-review")
    public ApplicationResponse reReview(@PathVariable Long id,
                                        @Valid @RequestBody ReReviewRequest request) {
        return applicationService.reReview(id, request);
    }

    /** 拨付：复核通过待拨付 -> 已拨付。 */
    @PostMapping("/{id}/disbursement")
    public ApplicationResponse disburse(@PathVariable Long id,
                                        @Valid @RequestBody DisburseRequest request) {
        return applicationService.disburse(id, request);
    }

    /** 查询申请的流转记录。 */
    @GetMapping("/{id}/flow-records")
    public List<FlowRecordResponse> flowRecords(@PathVariable Long id) {
        return applicationService.getFlowRecords(id);
    }

    /** 查询申请下的所有复核。 */
    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return reviewService.listByApplication(id);
    }
}
