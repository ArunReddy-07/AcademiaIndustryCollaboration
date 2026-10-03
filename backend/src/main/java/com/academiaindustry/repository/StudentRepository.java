package com.academiaindustry.repository;

import com.academiaindustry.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByUserEmailIgnoreCase(String email);

	boolean existsByIdAndUserEmailIgnoreCase(Long id, String email);

	boolean existsByInstitutionId(Long institutionId);

	@Query("select s from Student s join fetch s.user u join fetch s.institution i "
			+ "where i.id = :institutionId order by u.name")
	List<Student> findByInstitutionIdForFaculty(@Param("institutionId") Long institutionId);

	@Query("select case when count(s) > 0 then true else false end from Student s "
			+ "where s.id = :studentId and s.institution.id = "
			+ "(select f.facultyInstitution.id from User f where lower(f.email) = lower(:email))")
	boolean existsByIdAndFacultyInstitutionEmailIgnoreCase(
			@Param("studentId") Long studentId, @Param("email") String email);

	@Query("select s from Student s join fetch s.user u join fetch s.institution i join i.account a "
			+ "where lower(a.email) = lower(:email)")
	List<Student> findByInstitutionAccountEmailIgnoreCase(@Param("email") String email);

	@Query("select case when count(s) > 0 then true else false end from Student s join s.institution i "
			+ "join i.account a where s.id = :id and lower(a.email) = lower(:email)")
	boolean existsByIdAndInstitutionAccountEmailIgnoreCase(@Param("id") Long id, @Param("email") String email);
}