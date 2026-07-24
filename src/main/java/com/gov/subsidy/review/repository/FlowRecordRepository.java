package com.gov.subsidy.review.repository;

import com.gov.subsidy.review.domain.entity.FlowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowRecordRepository extends JpaRepository<FlowRecord, Long> {

    List<FlowRecord> findByApplicationIdOrderByOccurredAtAsc(Long applicationId);
}
