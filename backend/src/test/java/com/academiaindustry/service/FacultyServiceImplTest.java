package com.academiaindustry.service;

import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.impl.FacultyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacultyServiceImplTest {
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private FacultyServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        studentRepository = mock(StudentRepository.class);
        service = new FacultyServiceImpl(userRepository, studentRepository,
                mock(StudentSkillRepository.class), mock(ApplicationRepository.class),
                mock(OpportunityRepository.class), mock(InternshipRepository.class),
                mock(JobRepository.class));
    }

    @Test
    void listsOnlyStudentsBelongingToTheFacultyInstitution() {
        Institution institution = mock(Institution.class);
        User faculty = mock(User.class);
        when(faculty.getRole()).thenReturn(Role.FACULTY);
        when(faculty.getFacultyInstitution()).thenReturn(institution);
        when(institution.getId()).thenReturn(42L);
        when(userRepository.findByEmailIgnoreCase("faculty@example.com")).thenReturn(Optional.of(faculty));
        when(studentRepository.findByInstitutionIdForFaculty(42L)).thenReturn(List.of());

        service.getStudents("faculty@example.com");

        verify(studentRepository).findByInstitutionIdForFaculty(42L);
    }

    @Test
    void requiresInstitutionBeforeListingStudents() {
        User faculty = mock(User.class);
        when(faculty.getRole()).thenReturn(Role.FACULTY);
        when(faculty.getFacultyInstitution()).thenReturn(null);
        when(userRepository.findByEmailIgnoreCase("faculty@example.com")).thenReturn(Optional.of(faculty));

        assertThrows(BusinessRuleException.class, () -> service.getStudents("faculty@example.com"));
    }
}
