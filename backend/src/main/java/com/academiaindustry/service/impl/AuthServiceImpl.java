package com.academiaindustry.service.impl;

import com.academiaindustry.dto.AuthResponse;
import com.academiaindustry.dto.LoginRequest;
import com.academiaindustry.dto.RegisterRequest;
import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.security.JwtService;
import com.academiaindustry.service.AuthService;
import com.academiaindustry.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, UserService userService,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists.");
        }
        if (request.getRole() != Role.STUDENT
                && request.getRole() != Role.INDUSTRY
                && request.getRole() != Role.INSTITUTION) {
            throw new IllegalArgumentException("Only STUDENT, INDUSTRY, or INSTITUTION roles may register publicly.");
        }

        UserRequest userRequest = new UserRequest();
        userRequest.setName(request.getName());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(request.getPassword());
        userRequest.setRole(request.getRole());

        UserResponse userResponse;
        try {
            userResponse = userService.create(userRequest);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("A user with this email already exists.");
        }
        User user = findUser(request.getEmail());
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getExpirationMillis(), userResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = findUser(request.getEmail());
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getExpirationMillis(), toResponse(user));
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCompanyName(user.getCompanyName());
        response.setWebsite(user.getWebsite());
        response.setIndustrySector(user.getIndustrySector());
        response.setHeadquarters(user.getHeadquarters());
        response.setCompanyDescription(user.getCompanyDescription());
        if (user.getFacultyInstitution() != null) {
            response.setFacultyInstitutionId(user.getFacultyInstitution().getId());
            response.setFacultyInstitutionName(user.getFacultyInstitution().getName());
        }
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}