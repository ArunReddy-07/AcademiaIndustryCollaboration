package com.academiaindustry.service;

import com.academiaindustry.dto.CollaborationRequest;
import com.academiaindustry.entity.CollaborationStatus;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.CollaborationRepository;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.impl.CollaborationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollaborationServiceImplTest {

    private UserRepository userRepository;
    private InstitutionRepository institutionRepository;
    private CollaborationServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        institutionRepository = mock(InstitutionRepository.class);
        service = new CollaborationServiceImpl(mock(CollaborationRepository.class), userRepository,
                institutionRepository, mock(StudentRepository.class));
    }

    @Test
    void rejectsCreationByUnrelatedAuthenticatedUser() {
        User industry = mock(User.class);
        when(industry.getEmail()).thenReturn("industry@example.com");
        when(industry.getRole()).thenReturn(Role.INDUSTRY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(industry));
        when(institutionRepository.findById(2L)).thenReturn(Optional.of(mock(Institution.class)));

        CollaborationRequest request = new CollaborationRequest();
        request.setIndustryId(1L);
        request.setInstitutionId(2L);
        request.setTitle("Research project");
        request.setDescription("Description");
        request.setStatus(CollaborationStatus.REQUESTED);

        assertThrows(AccessDeniedException.class,
                () -> service.create(request, "unrelated@example.com", false));
    }

    @Test
    void rejectsMissingParticipantReferences() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        CollaborationRequest request = new CollaborationRequest();
        request.setIndustryId(1L);
        request.setInstitutionId(2L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(request, "industry@example.com", false));
    }
}
