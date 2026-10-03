package com.academiaindustry.repository;

import com.academiaindustry.entity.StudentSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {

	@EntityGraph(attributePaths = "skill")
	List<StudentSkill> findByStudentUserEmailIgnoreCase(String email);

	@Override
	@EntityGraph(attributePaths = "skill")
	List<StudentSkill> findAll();

	@Override
	@EntityGraph(attributePaths = "skill")
	java.util.Optional<StudentSkill> findById(Long id);

	boolean existsByStudentIdAndSkillId(Long studentId, Long skillId);

	@EntityGraph(attributePaths = "skill")
	List<StudentSkill> findByStudentId(Long studentId);

	List<StudentSkill> findBySkillId(Long skillId);

	@Query("select ss from StudentSkill ss join fetch ss.student s join fetch s.user u "
			+ "join fetch s.institution i join i.account a where ss.skill.id = :skillId "
			+ "and lower(a.email) = lower(:email)")
	List<StudentSkill> findBySkillIdAndStudentInstitutionAccountEmailIgnoreCase(
			@Param("skillId") Long skillId, @Param("email") String email);
}