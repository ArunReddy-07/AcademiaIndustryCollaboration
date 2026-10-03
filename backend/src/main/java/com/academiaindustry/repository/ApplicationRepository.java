package com.academiaindustry.repository;

import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentUserEmailIgnoreCase(String email);

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    boolean existsByStudentIdAndOpportunityTypeAndOpportunityId(
            Long studentId, OpportunityType opportunityType, Long opportunityId);

    List<Application> findByOpportunityTypeAndOpportunityId(
            OpportunityType opportunityType, Long opportunityId);

    List<Application> findByOpportunityTypeAndOpportunityIdAndStatus(
            OpportunityType opportunityType, Long opportunityId, ApplicationStatus status);

    List<Application> findBySharedPortfolioItems_Id(Long portfolioItemId);
}