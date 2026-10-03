package com.academiaindustry.service;

import com.academiaindustry.dto.StudentSkillRequest;
import com.academiaindustry.dto.StudentSkillResponse;

import java.util.List;

public interface StudentSkillService {

    StudentSkillResponse create(StudentSkillRequest request);

    StudentSkillResponse getById(Long id);

    List<StudentSkillResponse> getAll();

    List<StudentSkillResponse> getMine(String email);

    StudentSkillResponse update(Long id, StudentSkillRequest request);

    void delete(Long id);
}