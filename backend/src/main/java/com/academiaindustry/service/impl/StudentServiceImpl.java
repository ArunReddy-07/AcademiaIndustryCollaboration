package com.academiaindustry.service.impl;

import com.academiaindustry.dto.StudentRequest;
import com.academiaindustry.dto.StudentResponse;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final StudentSkillRepository studentSkillRepository;

    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              InstitutionRepository institutionRepository,
                              StudentSkillRepository studentSkillRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.studentSkillRepository = studentSkillRepository;
    }

    @Override
    public StudentResponse create(StudentRequest request) {
        User user = findUser(request.getUserId());
        if (user.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Only STUDENT accounts can own student profiles.");
        }
        Institution institution = findInstitution(request.getInstitutionId());
        Student student = new Student(user, institution, request.getBranch(), request.getGraduationYear(),
                request.getCgpa(), request.getCareerGoal(), request.getBio());
        updateProfileCompletion(student);
        return toResponse(studentRepository.save(student));
    }

    @Override
    public StudentResponse getById(Long id) {
        return toResponse(findStudent(id));
    }

    @Override
    public StudentResponse getByUserEmail(String email) {
        return toResponse(studentRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found.")));
    }

    @Override
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<StudentResponse> getAllBySkill(Long skillId) {
        List<Long> studentIds = studentSkillRepository.findBySkillId(skillId).stream()
                .map(studentSkill -> studentSkill.getStudent().getId())
                .distinct()
                .toList();
        return studentRepository.findAllById(studentIds).stream().map(this::toResponse).toList();
    }

    @Override
    public List<StudentResponse> getForInstitution(String email, Long skillId) {
        if (skillId == null) {
            return studentRepository.findByInstitutionAccountEmailIgnoreCase(email).stream()
                    .map(this::toResponse).toList();
        }
        return studentSkillRepository.findBySkillIdAndStudentInstitutionAccountEmailIgnoreCase(skillId, email).stream()
                .map(studentSkill -> studentSkill.getStudent())
                .distinct()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<StudentResponse> getForFaculty(String email, Long skillId) {
        User faculty = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty account not found."));
        if (faculty.getRole() != Role.FACULTY || faculty.getFacultyInstitution() == null) {
            throw new BusinessRuleException("Set your college or university before viewing students.");
        }
        List<Student> students = studentRepository.findByInstitutionIdForFaculty(
                faculty.getFacultyInstitution().getId());
        if (skillId == null) {
            return students.stream().map(this::toResponse).toList();
        }
        return students.stream()
                .filter(student -> studentSkillRepository.existsByStudentIdAndSkillId(student.getId(), skillId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudent(id);
        if (!student.getUser().getId().equals(request.getUserId())) {
            throw new BusinessRuleException("A student profile cannot be reassigned to another user.");
        }
        student.setInstitution(findInstitution(request.getInstitutionId()));
        student.setBranch(request.getBranch());
        student.setGraduationYear(request.getGraduationYear());
        student.setCgpa(request.getCgpa());
        student.setCareerGoal(request.getCareerGoal());
        student.setBio(request.getBio());
        updateProfileCompletion(student);
        return toResponse(studentRepository.save(student));
    }

    @Override
    public void delete(Long id) {
        studentRepository.delete(findStudent(id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Institution findInstitution(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + id));
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setUserId(student.getUser().getId());
        response.setUserName(student.getUser().getName());
        response.setUserEmail(student.getUser().getEmail());
        response.setInstitutionId(student.getInstitution().getId());
        response.setInstitutionName(student.getInstitution().getName());
        response.setBranch(student.getBranch());
        response.setGraduationYear(student.getGraduationYear());
        response.setCgpa(student.getCgpa());
        response.setCareerGoal(student.getCareerGoal());
        response.setBio(student.getBio());
        response.setProfileCompletionPercentage(profileCompletion(student));
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        return response;
    }

    private void updateProfileCompletion(Student student) {
        student.setProfileCompletionPercentage(profileCompletion(student));
    }

    private int profileCompletion(Student student) {
        int completedSections = 4;
        if (student.getCareerGoal() != null && !student.getCareerGoal().isBlank()) completedSections++;
        if (student.getBio() != null && !student.getBio().isBlank()) completedSections++;
        return completedSections * 100 / 6;
    }
}