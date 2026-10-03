package com.academiaindustry.service;

import com.academiaindustry.dto.StudentRequest;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class StudentServiceImplTest {

    @Test
    void rejectsChangingTheOwnerOfAnExistingStudentProfile() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        User user = mock(User.class);
        Student student = mock(Student.class);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(student.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(10L);

        StudentServiceImpl service = new StudentServiceImpl(
            studentRepository, mock(UserRepository.class), mock(InstitutionRepository.class),
            mock(StudentSkillRepository.class));
        StudentRequest request = new StudentRequest();
        request.setUserId(20L);

        assertThrows(BusinessRuleException.class, () -> service.update(1L, request));
        verify(studentRepository, never()).save(student);
    }

    @Test
    void filtersStudentProfilesBySkill() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        StudentSkillRepository studentSkillRepository = mock(StudentSkillRepository.class);
        Student student = mock(Student.class);
        StudentSkill studentSkill = mock(StudentSkill.class);
        User user = mock(User.class);
        Institution institution = mock(Institution.class);
        when(studentSkillRepository.findBySkillId(7L)).thenReturn(java.util.List.of(studentSkill));
        when(studentSkill.getStudent()).thenReturn(student);
        when(student.getId()).thenReturn(1L);
        when(studentRepository.findAllById(java.util.List.of(1L))).thenReturn(java.util.List.of(student));
        when(student.getUser()).thenReturn(user);
        when(student.getInstitution()).thenReturn(institution);
        when(user.getId()).thenReturn(2L);
        when(user.getName()).thenReturn("Student Name");
        when(user.getEmail()).thenReturn("student@example.com");
        when(institution.getId()).thenReturn(3L);
        when(institution.getName()).thenReturn("Example University");

        StudentServiceImpl service = new StudentServiceImpl(studentRepository, mock(UserRepository.class),
                mock(InstitutionRepository.class), studentSkillRepository);

        var results = service.getAllBySkill(7L);

        assertEquals(1, results.size());
        assertEquals("Student Name", results.get(0).getUserName());
        assertEquals("Example University", results.get(0).getInstitutionName());
        assertEquals(66, results.get(0).getProfileCompletionPercentage());
    }
}