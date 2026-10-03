package com.academiaindustry.service;

import com.academiaindustry.dto.SkillRequest;
import com.academiaindustry.dto.SkillResponse;

import java.util.List;

public interface SkillService {

    SkillResponse create(SkillRequest request);

    SkillResponse createForStudent(String name);

    SkillResponse getById(Long id);

    List<SkillResponse> getAll();

    SkillResponse update(Long id, SkillRequest request);

    void delete(Long id);
}