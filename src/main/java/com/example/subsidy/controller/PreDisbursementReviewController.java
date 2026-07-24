package com.example.subsidy.controller;

import com.example.subsidy.common.ApiResponse;
import com.example.subsidy.dto.ReviewCloseRequest;
import com.example.subsidy.dto.ReviewCreateRequest;
import com.example.subsidy.dto.ReviewDTO;
import com.example.subsidy.service.PreDisbursementReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PreDisbursementReviewController {

    private final PreDisbursementReviewService reviewService;

    @PostMapping("/application/{applicationId}")
    public ApiResponse<ReviewDTO> initiate(@PathVariable Long applicationId,
                                           @Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.ok(reviewService.initiateReview(applicationId, request));
    }

    @PostMapping("/{reviewId}/close")
    public ApiResponse<ReviewDTO> close(@PathVariable Long reviewId,
                                        @Valid @RequestBody ReviewCloseRequest request) {
        return ApiResponse.ok(reviewService.closeReview(reviewId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReviewDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(reviewService.findById(id));
    }

    @GetMapping("/application/{applicationId}")
    public ApiResponse<List<ReviewDTO>> listByApplication(@PathVariable Long applicationId) {
        return ApiResponse.ok(reviewService.findByApplication(applicationId));
    }
}
