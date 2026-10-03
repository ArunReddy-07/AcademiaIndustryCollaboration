package com.academiaindustry.service;

import com.academiaindustry.dto.SkillMatchResponse;
import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.entity.OpportunityStatus;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.CareerListingSkillRequirement;
import com.academiaindustry.entity.Internship;
import com.academiaindustry.entity.OpportunitySkillRequirement;
import com.academiaindustry.entity.ProficiencyLevel;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.OpportunitySkillRequirementRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.repository.CareerListingSkillRequirementRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.service.impl.SkillMatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillMatchingServiceImplTest {

    private StudentRepository studentRepository;
    private StudentSkillRepository studentSkillRepository;
    private OpportunityRepository opportunityRepository;
    private OpportunitySkillRequirementRepository requirementRepository;
    private CareerListingSkillRequirementRepository careerRequirementRepository;
    private InternshipRepository internshipRepository;
    private JobRepository jobRepository;
    private SkillMatchingServiceImpl service;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        studentSkillRepository = mock(StudentSkillRepository.class);
        opportunityRepository = mock(OpportunityRepository.class);
        requirementRepository = mock(OpportunitySkillRequirementRepository.class);
        careerRequirementRepository = mock(CareerListingSkillRequirementRepository.class);
        internshipRepository = mock(InternshipRepository.class);
        jobRepository = mock(JobRepository.class);
        service = new SkillMatchingServiceImpl(studentRepository, studentSkillRepository,
            opportunityRepository, requirementRepository, careerRequirementRepository,
            internshipRepository, jobRepository);

        when(studentRepository.findByUserEmailIgnoreCase("student@example.com"))
                .thenReturn(Optional.of(mock(Student.class)));
        Opportunity firstOpportunity = opportunity(1L, OpportunityType.PROJECT, OpportunityStatus.OPEN);
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(firstOpportunity));
    }

    @Test
    void returnsFullMatchWhenStudentMeetsAllRequirements() {
        Skill java = skill("Java");
        when(studentSkillRepository.findByStudentUserEmailIgnoreCase("student@example.com"))
                .thenReturn(List.of(studentSkill(java, ProficiencyLevel.ADVANCED)));
        when(requirementRepository.findByOpportunityId(1L))
                .thenReturn(List.of(requirement(java, ProficiencyLevel.INTERMEDIATE)));

        SkillMatchResponse result = service.match("student@example.com", 1L);

        assertEquals(1, result.getMatchedCount());
        assertEquals(100, result.getMatchPercentage());
        assertEquals(List.of(), result.getMissingSkills());
        assertEquals(List.of(), result.getInsufficientProficiencySkills());
    }

    @Test
    void separatesMissingAndInsufficientSkills() {
        Skill java = skill("Java");
        Skill sql = skill("SQL");
        Skill aws = skill("AWS");
        when(studentSkillRepository.findByStudentUserEmailIgnoreCase("student@example.com"))
                .thenReturn(List.of(studentSkill(java, ProficiencyLevel.EXPERT),
                        studentSkill(sql, ProficiencyLevel.BEGINNER)));
        when(requirementRepository.findByOpportunityId(1L))
                .thenReturn(List.of(requirement(java, ProficiencyLevel.INTERMEDIATE),
                        requirement(sql, ProficiencyLevel.ADVANCED),
                        requirement(aws, ProficiencyLevel.BEGINNER)));

        SkillMatchResponse result = service.match("student@example.com", 1L);

        assertEquals(1, result.getMatchedCount());
        assertEquals(3, result.getRequiredCount());
        assertEquals(33, result.getMatchPercentage());
        assertEquals(List.of("AWS"), result.getMissingSkills());
        assertEquals(List.of("SQL"), result.getInsufficientProficiencySkills());
    }

    @Test
    void returnsZeroForEmptyRequirementsAndNoStudentSkills() {
        when(studentSkillRepository.findByStudentUserEmailIgnoreCase("student@example.com"))
                .thenReturn(List.of());
        when(requirementRepository.findByOpportunityId(1L)).thenReturn(List.of());

        SkillMatchResponse result = service.match("student@example.com", 1L);

        assertEquals(0, result.getRequiredCount());
        assertEquals(0, result.getMatchPercentage());
    }

        @Test
        void matchesInternshipRequirementsUsingTheStudentSkillMap() {
        Skill java = skill("Java");
        Internship internship = mock(Internship.class);
        when(internshipRepository.findById(9L)).thenReturn(Optional.of(internship));
        when(studentSkillRepository.findByStudentUserEmailIgnoreCase("student@example.com"))
            .thenReturn(List.of(studentSkill(java, ProficiencyLevel.ADVANCED)));
        when(careerRequirementRepository.findByInternshipIdOrderById(9L))
            .thenReturn(List.of(new CareerListingSkillRequirement(internship, null, java,
                ProficiencyLevel.INTERMEDIATE)));

        SkillMatchResponse result = service.match("student@example.com", OpportunityType.INTERNSHIP, 9L);

        assertEquals(OpportunityType.INTERNSHIP, result.getOpportunityType());
        assertEquals(100, result.getMatchPercentage());
        assertEquals(List.of("Java"), result.getMatchedSkills());
        }

        @Test
        void recommendsOpenOpportunitiesInMatchOrder() {
        Skill java = skill("Java");
        Opportunity bestMatch = opportunity(1L, OpportunityType.PROJECT, OpportunityStatus.OPEN);
        Opportunity weakerMatch = opportunity(2L, OpportunityType.PROJECT, OpportunityStatus.OPEN);
        Opportunity closed = opportunity(3L, OpportunityType.PROJECT, OpportunityStatus.CLOSED);
        when(opportunityRepository.findAll()).thenReturn(List.of(bestMatch, weakerMatch, closed));
        when(opportunityRepository.findById(2L)).thenReturn(Optional.of(weakerMatch));
        when(studentSkillRepository.findByStudentUserEmailIgnoreCase("student@example.com"))
            .thenReturn(List.of(studentSkill(java, ProficiencyLevel.ADVANCED)));
        when(requirementRepository.findByOpportunityId(1L))
            .thenReturn(List.of(requirement(java, ProficiencyLevel.INTERMEDIATE)));
        when(requirementRepository.findByOpportunityId(2L))
            .thenReturn(List.of(requirement(java, ProficiencyLevel.EXPERT)));

        List<SkillMatchResponse> results = service.recommendations("student@example.com");

        assertEquals(List.of(1L, 2L), results.stream().map(SkillMatchResponse::getOpportunityId).toList());
        assertEquals(List.of(100, 0), results.stream().map(SkillMatchResponse::getMatchPercentage).toList());
        }

    private Skill skill(String name) {
        return new Skill(name, "Test", null);
    }

    private StudentSkill studentSkill(Skill skill, ProficiencyLevel level) {
        return new StudentSkill(null, skill, level);
    }

    private OpportunitySkillRequirement requirement(Skill skill, ProficiencyLevel level) {
        return new OpportunitySkillRequirement(null, skill, level);
    }

    private Opportunity opportunity(Long id, OpportunityType type, OpportunityStatus status) {
        Opportunity opportunity = mock(Opportunity.class);
        when(opportunity.getId()).thenReturn(id);
        when(opportunity.getType()).thenReturn(type);
        when(opportunity.getStatus()).thenReturn(status);
        when(opportunity.getApplicationDeadline()).thenReturn(LocalDate.now().plusDays(3));
        return opportunity;
    }
}
