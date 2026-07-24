package com.example.subsidy.controller;

import com.example.subsidy.dto.request.ReviewCompleteRequest;
import com.example.subsidy.dto.request.ReviewInitiateRequest;
import com.example.subsidy.dto.response.ApiResponse;
import com.example.subsidy.dto.response.ReviewResponse;
import com.example.subsidy.service.SubsidyReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class SubsidyReviewController {

    private final SubsidyReviewService reviewService;

    public SubsidyReviewController(SubsidyReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/application/{applicationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> initiateReview(
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewInitiateRequest request) {
        return ApiResponse.success(reviewService.initiateReview(applicationId, request));
    }

    @PutMapping("/{reviewId}/complete")
    public ApiResponse<ReviewResponse> completeReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewCompleteRequest request) {
        return ApiResponse.success(reviewService.completeReview(reviewId, request));
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> getReview(@PathVariable Long reviewId) {
        return ApiResponse.success(reviewService.getReview(reviewId));
    }

    @GetMapping("/application/{applicationId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByApplication(@PathVariable Long applicationId) {
        return ApiResponse.success(reviewService.getReviewsByApplication(applicationId));
    }
}
