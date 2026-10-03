package com.academiaindustry.service.impl;

import com.academiaindustry.dto.CollaborationRequest;
import com.academiaindustry.dto.CollaborationResponse;
import com.academiaindustry.entity.Collaboration;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.User;
import com.academiaindustry.entity.CollaborationStatus;
import com.academiaindustry.entity.Role;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.CollaborationRepository;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.CollaborationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CollaborationServiceImpl implements CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;

    public CollaborationServiceImpl(CollaborationRepository collaborationRepository, UserRepository userRepository,
                                     InstitutionRepository institutionRepository, StudentRepository studentRepository) {
        this.collaborationRepository = collaborationRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public CollaborationResponse create(CollaborationRequest request, String email, boolean administrator) {
        User industry = findUser(request.getIndustryId());
        requireRole(industry, Role.INDUSTRY, "industry");
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + request.getInstitutionId()));
        User academician = request.getAcademicianId() == null ? null : findUser(request.getAcademicianId());
        if (academician != null && academician.getRole() != Role.FACULTY
                && academician.getRole() != Role.ACADEMICIAN) {
            throw new IllegalArgumentException("The academician participant must have an academic role.");
        }
        Student student = request.getStudentId() == null ? null : studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));
        if (student != null && student.getUser().getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("The student participant must have the STUDENT role.");
        }
        if (!administrator && !isParticipant(email, industry, institution, academician, student)) {
            throw new AccessDeniedException("You must be a collaboration participant to create this request.");
        }
        Collaboration collaboration = new Collaboration(industry, institution, academician, student,
                request.getTitle(), request.getDescription(), request.getStatus());
        return toResponse(collaborationRepository.save(collaboration));
    }

    @Override
    public CollaborationResponse getById(Long id, String email, boolean administrator) {
        Collaboration collaboration = findCollaboration(id);
        authorizeParticipant(collaboration, email, administrator);
        return toResponse(collaboration);
    }

    @Override
    public List<CollaborationResponse> getAll(String email, boolean administrator) {
        return collaborationRepository.findAll().stream()
                .filter(collaboration -> administrator || isParticipant(email, collaboration))
                .map(this::toResponse).toList();
    }

    @Override
    public CollaborationResponse updateStatus(Long id, CollaborationStatus status,
                                              String email, boolean administrator) {
        Collaboration collaboration = findCollaboration(id);
        authorizeParticipant(collaboration, email, administrator);
        collaboration.setStatus(status);
        return toResponse(collaborationRepository.save(collaboration));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void requireRole(User user, Role role, String participant) {
        if (user.getRole() != role) {
            throw new IllegalArgumentException("The " + participant + " participant has an invalid role.");
        }
    }

    private Collaboration findCollaboration(Long id) {
        return collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found with id: " + id));
    }

    private void authorizeParticipant(Collaboration collaboration, String email, boolean administrator) {
        if (!administrator && !isParticipant(email, collaboration)) {
            throw new AccessDeniedException("You are not a participant in this collaboration.");
        }
    }

    private boolean isParticipant(String email, Collaboration collaboration) {
        return isParticipant(email, collaboration.getIndustry(), collaboration.getInstitution(),
                collaboration.getAcademician(), collaboration.getStudent());
    }

    private boolean isParticipant(String email, User industry, Institution institution,
                                  User academician, Student student) {
        return industry.getEmail().equalsIgnoreCase(email)
                || (institution.getAccount() != null && institution.getAccount().getEmail().equalsIgnoreCase(email))
                || (academician != null && academician.getEmail().equalsIgnoreCase(email))
                || (student != null && student.getUser().getEmail().equalsIgnoreCase(email));
    }

    private CollaborationResponse toResponse(Collaboration collaboration) {
        CollaborationResponse response = new CollaborationResponse();
        response.setId(collaboration.getId());
        response.setIndustryId(collaboration.getIndustry().getId());
        response.setInstitutionId(collaboration.getInstitution().getId());
        response.setAcademicianId(collaboration.getAcademician() == null ? null : collaboration.getAcademician().getId());
        response.setStudentId(collaboration.getStudent() == null ? null : collaboration.getStudent().getId());
        response.setTitle(collaboration.getTitle());
        response.setDescription(collaboration.getDescription());
        response.setStatus(collaboration.getStatus());
        response.setCreatedAt(collaboration.getCreatedAt());
        response.setUpdatedAt(collaboration.getUpdatedAt());
        return response;
    }
}