package com.academiaindustry.repository;

import com.academiaindustry.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
}