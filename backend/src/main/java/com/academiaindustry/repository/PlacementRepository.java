package com.academiaindustry.repository;

import com.academiaindustry.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacementRepository extends JpaRepository<Placement, Long> {

    Optional<Placement> findByApplicationId(Long applicationId);
}