package com.example.subsidy.repository;

import com.example.subsidy.entity.SubsidyApplication;
import com.example.subsidy.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {

    List<SubsidyApplication> findByStatus(ApplicationStatus status);
}
