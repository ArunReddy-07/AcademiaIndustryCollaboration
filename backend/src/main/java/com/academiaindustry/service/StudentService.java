package com.academiaindustry.service;

import com.academiaindustry.dto.StudentRequest;
import com.academiaindustry.dto.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse create(StudentRequest request);

    StudentResponse getById(Long id);

    StudentResponse getByUserEmail(String email);

    List<StudentResponse> getAll();

    List<StudentResponse> getAllBySkill(Long skillId);

    List<StudentResponse> getForInstitution(String email, Long skillId);

    List<StudentResponse> getForFaculty(String email, Long skillId);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);
}