package com.academiaindustry.repository;

import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.entity.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findByType(OpportunityType type);

    boolean existsByIdAndIndustryEmailIgnoreCase(Long id, String email);
}