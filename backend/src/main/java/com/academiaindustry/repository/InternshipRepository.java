package com.academiaindustry.repository;

import com.academiaindustry.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipRepository extends JpaRepository<Internship, Long> {

	boolean existsByIdAndIndustryEmailIgnoreCase(Long id, String email);
}