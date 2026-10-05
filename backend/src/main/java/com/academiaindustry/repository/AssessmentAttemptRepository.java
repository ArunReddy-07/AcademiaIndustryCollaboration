package com.academiaindustry.repository;

import com.academiaindustry.entity.AssessmentAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    @Query("select attempt from AssessmentAttempt attempt join attempt.student student "
            + "join student.user user where attempt.id = :attemptId "
            + "and lower(user.email) = lower(:email)")
    Optional<AssessmentAttempt> findByIdAndStudentEmailIgnoreCase(
            @Param("attemptId") Long attemptId, @Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from AssessmentAttempt attempt join fetch attempt.student student "
            + "join student.user user where attempt.id = :attemptId "
            + "and lower(user.email) = lower(:email)")
    Optional<AssessmentAttempt> findByIdAndStudentEmailIgnoreCaseForUpdate(
            @Param("attemptId") Long attemptId, @Param("email") String email);
}
