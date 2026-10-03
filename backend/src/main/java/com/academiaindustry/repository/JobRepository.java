package com.academiaindustry.repository;

import com.academiaindustry.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

	boolean existsByIdAndIndustryEmailIgnoreCase(Long id, String email);
}