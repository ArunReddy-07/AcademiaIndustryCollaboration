package com.academiaindustry.service;

import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.dto.ProfileUpdateRequest;
import com.academiaindustry.entity.Role;

import java.util.List;

public interface UserService {

    UserResponse create(UserRequest request);

    UserResponse getById(Long id);

    UserResponse getByEmail(String email);

    UserResponse updateProfile(String email, ProfileUpdateRequest request);

    UserResponse updateFacultyInstitution(String email, String institutionName);

    List<UserResponse> getAll();

    List<UserResponse> getByRole(Role role);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);
}