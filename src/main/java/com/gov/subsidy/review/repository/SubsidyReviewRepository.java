package com.gov.subsidy.review.repository;

import com.gov.subsidy.review.domain.entity.SubsidyReview;
import com.gov.subsidy.review.domain.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubsidyReviewRepository extends JpaRepository<SubsidyReview, Long> {

    /** 查询某申请下指定状态的复核，用于判断是否存在未关闭复核。 */
    Optional<SubsidyReview> findByApplicationIdAndReviewStatus(Long applicationId, ReviewStatus reviewStatus);

    boolean existsByApplicationIdAndReviewStatus(Long applicationId, ReviewStatus reviewStatus);

    List<SubsidyReview> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
