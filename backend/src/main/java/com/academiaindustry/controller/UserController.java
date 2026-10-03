package com.academiaindustry.controller;

import com.academiaindustry.dto.ProfileUpdateRequest;
import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.dto.InstitutionRequest;
import com.academiaindustry.entity.Role;
import com.academiaindustry.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(Authentication authentication,
                                          @Valid @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @PutMapping("/me/faculty-institution")
    @PreAuthorize("hasRole('FACULTY')")
    public UserResponse updateFacultyInstitution(Authentication authentication,
                                                 @Valid @RequestBody InstitutionRequest request) {
        return userService.updateFacultyInstitution(authentication.getName(), request.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/industry-directory")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'ACADEMICIAN', 'INSTITUTION')")
    public List<UserResponse> industryDirectory() {
        return userService.getByRole(Role.INDUSTRY);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isUser(authentication.name, #id)")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}