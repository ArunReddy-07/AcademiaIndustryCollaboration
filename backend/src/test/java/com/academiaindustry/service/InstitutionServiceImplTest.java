package com.academiaindustry.service;

import com.academiaindustry.entity.Institution;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.service.impl.InstitutionServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InstitutionServiceImplTest {

    @Test
    void preventsDeletingAnInstitutionUsedByStudentProfiles() {
        InstitutionRepository institutionRepository = mock(InstitutionRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        Institution institution = mock(Institution.class);
        when(institutionRepository.findById(5L)).thenReturn(Optional.of(institution));
        when(studentRepository.existsByInstitutionId(5L)).thenReturn(true);
        InstitutionServiceImpl service = new InstitutionServiceImpl(institutionRepository, studentRepository);

        assertThrows(BusinessRuleException.class, () -> service.delete(5L));
        verify(institutionRepository, never()).delete(institution);
    }
}