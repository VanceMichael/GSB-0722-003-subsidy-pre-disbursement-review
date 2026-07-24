package com.gov.subsidy.review.controller;

import com.gov.subsidy.review.dto.DecideReviewRequest;
import com.gov.subsidy.review.dto.ReviewResponse;
import com.gov.subsidy.review.dto.StartReviewRequest;
import com.gov.subsidy.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 复核相关接口。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 对指定申请发起复核。 */
    @PostMapping("/applications/{applicationId}/reviews")
    public ResponseEntity<ReviewResponse> startReview(@PathVariable Long applicationId,
                                                      @Valid @RequestBody StartReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.startReview(applicationId, request));
    }

    /** 对复核做出结论（通过/退回）。 */
    @PostMapping("/reviews/{reviewId}/decision")
    public ReviewResponse decide(@PathVariable Long reviewId,
                                 @Valid @RequestBody DecideReviewRequest request) {
        return reviewService.decide(reviewId, request);
    }
}
