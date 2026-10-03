package com.academiaindustry.service.impl;

import com.academiaindustry.dto.FacultyApplicationSummary;
import com.academiaindustry.dto.FacultyStudentOverview;
import com.academiaindustry.dto.StudentResponse;
import com.academiaindustry.dto.StudentSkillResponse;
import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.FacultyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacultyServiceImpl implements FacultyService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final InternshipRepository internshipRepository;
    private final JobRepository jobRepository;

    public FacultyServiceImpl(UserRepository userRepository, StudentRepository studentRepository,
                              StudentSkillRepository studentSkillRepository,
                              ApplicationRepository applicationRepository,
                              OpportunityRepository opportunityRepository,
                              InternshipRepository internshipRepository,
                              JobRepository jobRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.internshipRepository = internshipRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<FacultyStudentOverview> getStudents(String facultyEmail) {
        User faculty = userRepository.findByEmailIgnoreCase(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty account not found."));
        if (faculty.getRole() != Role.FACULTY || faculty.getFacultyInstitution() == null) {
            throw new BusinessRuleException("Set your college or university before viewing students.");
        }
        Long institutionId = faculty.getFacultyInstitution().getId();
        return studentRepository.findByInstitutionIdForFaculty(institutionId).stream()
                .map(this::toOverview)
                .toList();
    }

    private FacultyStudentOverview toOverview(Student student) {
        FacultyStudentOverview overview = new FacultyStudentOverview();
        overview.setStudent(toStudentResponse(student));
        overview.setSkills(studentSkillRepository.findByStudentId(student.getId()).stream()
                .map(this::toStudentSkillResponse).toList());
        overview.setApplications(applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId()).stream()
                .map(this::toApplicationSummary).toList());
        return overview;
    }

    private StudentResponse toStudentResponse(Student student) {
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
        response.setProfileCompletionPercentage(student.getProfileCompletionPercentage());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        return response;
    }

    private StudentSkillResponse toStudentSkillResponse(StudentSkill studentSkill) {
        StudentSkillResponse response = new StudentSkillResponse();
        response.setId(studentSkill.getId());
        response.setStudentId(studentSkill.getStudent().getId());
        response.setSkillId(studentSkill.getSkill().getId());
        response.setSkillName(studentSkill.getSkill().getName());
        response.setProficiencyLevel(studentSkill.getProficiencyLevel());
        return response;
    }

    private FacultyApplicationSummary toApplicationSummary(Application application) {
        FacultyApplicationSummary response = new FacultyApplicationSummary();
        response.setId(application.getId());
        response.setOpportunityType(application.getOpportunityType());
        response.setOpportunityId(application.getOpportunityId());
        response.setOpportunityTitle(opportunityTitle(application.getOpportunityType(), application.getOpportunityId()));
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        return response;
    }

    private String opportunityTitle(OpportunityType type, Long id) {
        return switch (type) {
            case PROJECT, APPRENTICESHIP, PROGRAM -> opportunityRepository.findById(id.longValue())
                    .map(opportunity -> opportunity.getTitle()).orElse("Opportunity " + id);
            case INTERNSHIP -> internshipRepository.findById(id.longValue())
                    .map(internship -> internship.getTitle()).orElse("Internship " + id);
            case JOB -> jobRepository.findById(id.longValue())
                    .map(job -> job.getTitle()).orElse("Job " + id);
        };
    }
}
