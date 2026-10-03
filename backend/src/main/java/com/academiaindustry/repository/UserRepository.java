package com.academiaindustry.repository;

import com.academiaindustry.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.academiaindustry.entity.Role;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	List<User> findByRoleOrderByNameAsc(Role role);
}