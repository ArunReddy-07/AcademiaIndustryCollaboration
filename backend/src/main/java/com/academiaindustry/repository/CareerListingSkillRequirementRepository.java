package com.academiaindustry.repository;

import com.academiaindustry.entity.CareerListingSkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CareerListingSkillRequirementRepository
        extends JpaRepository<CareerListingSkillRequirement, Long> {

        @Query("select r from CareerListingSkillRequirement r where r.internship.id = :internshipId order by r.id")
        List<CareerListingSkillRequirement> findByInternshipIdOrderById(@Param("internshipId") Long internshipId);

        @Query("select r from CareerListingSkillRequirement r where r.job.id = :jobId order by r.id")
        List<CareerListingSkillRequirement> findByJobIdOrderById(@Param("jobId") Long jobId);

        @Query("select case when count(r) > 0 then true else false end from CareerListingSkillRequirement r "
            + "where r.internship.id = :internshipId and r.skill.id = :skillId")
        boolean existsByInternshipIdAndSkillId(@Param("internshipId") Long internshipId,
                           @Param("skillId") Long skillId);

        @Query("select case when count(r) > 0 then true else false end from CareerListingSkillRequirement r "
            + "where r.job.id = :jobId and r.skill.id = :skillId")
        boolean existsByJobIdAndSkillId(@Param("jobId") Long jobId, @Param("skillId") Long skillId);

        @Query("select r from CareerListingSkillRequirement r where r.id = :id and r.internship.id = :internshipId")
        Optional<CareerListingSkillRequirement> findByIdAndInternshipId(@Param("id") Long id,
                                        @Param("internshipId") Long internshipId);

        @Query("select r from CareerListingSkillRequirement r where r.id = :id and r.job.id = :jobId")
        Optional<CareerListingSkillRequirement> findByIdAndJobId(@Param("id") Long id, @Param("jobId") Long jobId);
}