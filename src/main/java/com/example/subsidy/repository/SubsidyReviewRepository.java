package com.example.subsidy.repository;

import com.example.subsidy.entity.SubsidyReview;
import com.example.subsidy.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubsidyReviewRepository extends JpaRepository<SubsidyReview, Long> {

    List<SubsidyReview> findByApplicationIdOrderByInitiatedAtDesc(Long applicationId);

    Optional<SubsidyReview> findByApplicationIdAndStatus(Long applicationId, ReviewStatus status);

    boolean existsByApplicationIdAndStatus(Long applicationId, ReviewStatus status);
}
