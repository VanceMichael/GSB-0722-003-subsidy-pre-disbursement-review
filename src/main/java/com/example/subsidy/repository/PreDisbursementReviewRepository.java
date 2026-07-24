package com.example.subsidy.repository;

import com.example.subsidy.entity.PreDisbursementReview;
import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreDisbursementReviewRepository extends JpaRepository<PreDisbursementReview, Long> {

    List<PreDisbursementReview> findByApplicationOrderByCreatedAtDesc(SubsidyApplication application);

    Optional<PreDisbursementReview> findByApplicationAndStatus(SubsidyApplication application, ReviewStatus status);

    boolean existsByApplicationAndStatus(SubsidyApplication application, ReviewStatus status);
}
