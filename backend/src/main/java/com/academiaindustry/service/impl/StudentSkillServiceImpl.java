package com.academiaindustry.service.impl;

import com.academiaindustry.dto.StudentSkillRequest;
import com.academiaindustry.dto.StudentSkillResponse;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.service.StudentSkillService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentSkillServiceImpl implements StudentSkillService {

    private final StudentSkillRepository studentSkillRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;

    public StudentSkillServiceImpl(StudentSkillRepository studentSkillRepository,
                                   StudentRepository studentRepository,
                                   SkillRepository skillRepository) {
        this.studentSkillRepository = studentSkillRepository;
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public StudentSkillResponse create(StudentSkillRequest request) {
        Student student = findStudent(request.getStudentId());
        Skill skill = findSkill(request.getSkillId());
        if (studentSkillRepository.existsByStudentIdAndSkillId(student.getId(), skill.getId())) {
            throw new DuplicateResourceException("This skill is already on your student profile.");
        }
        StudentSkill studentSkill = new StudentSkill(student, skill, request.getProficiencyLevel());
        return toResponse(studentSkillRepository.save(studentSkill));
    }

    @Override
    public StudentSkillResponse getById(Long id) {
        return toResponse(findStudentSkill(id));
    }

    @Override
    public List<StudentSkillResponse> getAll() {
        return studentSkillRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<StudentSkillResponse> getMine(String email) {
        return studentSkillRepository.findByStudentUserEmailIgnoreCase(email)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public StudentSkillResponse update(Long id, StudentSkillRequest request) {
        StudentSkill studentSkill = findStudentSkill(id);
        studentSkill.setStudent(findStudent(request.getStudentId()));
        studentSkill.setSkill(findSkill(request.getSkillId()));
        studentSkill.setProficiencyLevel(request.getProficiencyLevel());
        return toResponse(studentSkillRepository.save(studentSkill));
    }

    @Override
    public void delete(Long id) {
        studentSkillRepository.delete(findStudentSkill(id));
    }

    private StudentSkill findStudentSkill(Long id) {
        return studentSkillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student skill not found with id: " + id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Skill findSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    private StudentSkillResponse toResponse(StudentSkill studentSkill) {
        StudentSkillResponse response = new StudentSkillResponse();
        response.setId(studentSkill.getId());
        response.setStudentId(studentSkill.getStudent().getId());
        response.setSkillId(studentSkill.getSkill().getId());
        response.setSkillName(studentSkill.getSkill().getName());
        response.setProficiencyLevel(studentSkill.getProficiencyLevel());
        return response;
    }
}