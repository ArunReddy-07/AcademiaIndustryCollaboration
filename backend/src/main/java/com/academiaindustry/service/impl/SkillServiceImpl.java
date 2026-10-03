package com.academiaindustry.service.impl;

import com.academiaindustry.dto.SkillRequest;
import com.academiaindustry.dto.SkillResponse;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.service.SkillService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public SkillResponse create(SkillRequest request) {
        Skill skill = new Skill(request.getName(), request.getCategory(), request.getDescription());
        return toResponse(skillRepository.save(skill));
    }

    @Override
    public SkillResponse createForStudent(String name) {
        String normalizedName = name.trim();
        Skill skill = skillRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> skillRepository.save(new Skill(normalizedName, "Student-added", null)));
        return toResponse(skill);
    }

    @Override
    public SkillResponse getById(Long id) {
        return toResponse(findSkill(id));
    }

    @Override
    public List<SkillResponse> getAll() {
        return skillRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = findSkill(id);
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        return toResponse(skillRepository.save(skill));
    }

    @Override
    public void delete(Long id) {
        skillRepository.delete(findSkill(id));
    }

    private Skill findSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    private SkillResponse toResponse(Skill skill) {
        SkillResponse response = new SkillResponse();
        response.setId(skill.getId());
        response.setName(skill.getName());
        response.setCategory(skill.getCategory());
        response.setDescription(skill.getDescription());
        return response;
    }
}