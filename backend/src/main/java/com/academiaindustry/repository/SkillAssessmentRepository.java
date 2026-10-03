package com.academiaindustry.repository;

import com.academiaindustry.entity.SkillAssessment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillAssessmentRepository extends JpaRepository<SkillAssessment, Long> {

    List<SkillAssessment> findByStudentUserEmailIgnoreCase(String email);

    Optional<SkillAssessment> findByStudentIdAndSkillId(Long studentId, Long skillId);

    @Modifying
    @Query(value = """
            INSERT INTO skill_assessments (student_id, skill_id, score, level, assessed_at)
            VALUES (:studentId, :skillId, :score, :level, CURRENT_TIMESTAMP)
            ON CONFLICT (student_id, skill_id)
            DO UPDATE SET score = EXCLUDED.score, level = EXCLUDED.level
            """, nativeQuery = true)
    int saveOrUpdateScore(@Param("studentId") Long studentId,
                          @Param("skillId") Long skillId,
                          @Param("score") Integer score,
                          @Param("level") String level);
}