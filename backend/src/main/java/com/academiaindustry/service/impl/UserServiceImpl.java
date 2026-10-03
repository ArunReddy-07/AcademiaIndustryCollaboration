package com.academiaindustry.service.impl;

import com.academiaindustry.dto.ProfileUpdateRequest;
import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.dto.UserResponse;
import com.academiaindustry.entity.User;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.academiaindustry.entity.Role;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InstitutionRepository institutionRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.institutionRepository = institutionRepository;
    }

    @Override
    public UserResponse create(UserRequest request) {
        User user = new User(request.getName(), request.getEmail(),
            passwordEncoder.encode(request.getPassword()), request.getRole());
        User saved = userRepository.save(user);
        if (saved.getRole() == com.academiaindustry.entity.Role.INSTITUTION) {
            institutionRepository.save(new Institution(saved.getName(), null, null, null, saved));
        }
        return toResponse(saved);
    }

    @Override
    public UserResponse getById(Long id) {
        return toResponse(findUser(id));
    }

    @Override
    public UserResponse getByEmail(String email) {
        return toResponse(userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.")));
    }

    @Override
    public UserResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new com.academiaindustry.exception.DuplicateResourceException(
                    "A user with this email already exists.");
        }
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCompanyName(request.getCompanyName());
        user.setWebsite(request.getWebsite());
        user.setIndustrySector(request.getIndustrySector());
        user.setHeadquarters(request.getHeadquarters());
        user.setCompanyDescription(request.getCompanyDescription());
        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateFacultyInstitution(String email, String institutionName) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getRole() != Role.FACULTY) {
            throw new com.academiaindustry.exception.BusinessRuleException(
                    "Only faculty accounts can set a faculty institution.");
        }
        String normalizedName = institutionName == null ? "" : institutionName.trim();
        if (normalizedName.isBlank()) {
            throw new com.academiaindustry.exception.BusinessRuleException("College or university name is required.");
        }
        Institution institution = institutionRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> institutionRepository.save(new Institution(normalizedName)));
        user.setFacultyInstitution(institution);
        return toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<UserResponse> getByRole(Role role) {
        return userRepository.findByRoleOrderByNameAsc(role).stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = findUser(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        User saved = userRepository.save(user);
        if (saved.getRole() == com.academiaindustry.entity.Role.INSTITUTION) {
            Institution institution = institutionRepository.findByAccountId(saved.getId())
                    .orElseGet(() -> new Institution(saved.getName(), null, null, null, saved));
            institution.setName(saved.getName());
            institutionRepository.save(institution);
        } else {
            institutionRepository.findByAccountId(saved.getId()).ifPresent(institution -> {
                institution.setAccount(null);
                institutionRepository.save(institution);
            });
        }
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
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