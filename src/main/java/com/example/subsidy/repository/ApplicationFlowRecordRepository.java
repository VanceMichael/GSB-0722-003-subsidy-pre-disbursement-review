package com.example.subsidy.repository;

import com.example.subsidy.entity.ApplicationFlowRecord;
import com.example.subsidy.entity.SubsidyApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationFlowRecordRepository extends JpaRepository<ApplicationFlowRecord, Long> {

    List<ApplicationFlowRecord> findByApplicationOrderByCreatedAtAsc(SubsidyApplication application);
}
