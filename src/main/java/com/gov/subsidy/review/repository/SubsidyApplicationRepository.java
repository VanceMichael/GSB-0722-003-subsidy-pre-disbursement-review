package com.gov.subsidy.review.repository;

import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {
}
