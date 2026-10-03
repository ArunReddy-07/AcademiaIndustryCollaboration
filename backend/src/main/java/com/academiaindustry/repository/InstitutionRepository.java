package com.academiaindustry.repository;

import com.academiaindustry.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

	@Query("select i from Institution i join i.account a where lower(a.email) = lower(:email)")
	Optional<Institution> findByAccountEmailIgnoreCase(@Param("email") String email);

	Optional<Institution> findByAccountId(Long accountId);

	Optional<Institution> findByNameIgnoreCase(String name);

	@Query("select case when count(i) > 0 then true else false end from Institution i "
			+ "join i.account a where i.id = :id and lower(a.email) = lower(:email)")
	boolean existsByIdAndAccountEmailIgnoreCase(@Param("id") Long id, @Param("email") String email);
}