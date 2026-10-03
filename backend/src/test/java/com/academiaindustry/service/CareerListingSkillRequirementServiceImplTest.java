package com.academiaindustry.service;

import com.academiaindustry.dto.SkillRequirementRequest;
import com.academiaindustry.dto.CareerSkillRequirementUpdateRequest;
import com.academiaindustry.entity.CareerListingSkillRequirement;
import com.academiaindustry.entity.Internship;
import com.academiaindustry.entity.Job;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.ProficiencyLevel;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.CareerListingSkillRequirementRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.service.impl.CareerListingSkillRequirementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CareerListingSkillRequirementServiceImplTest {

    private CareerListingSkillRequirementRepository requirementRepository;
    private InternshipRepository internshipRepository;
    private JobRepository jobRepository;
    private SkillRepository skillRepository;
    private CareerListingSkillRequirementServiceImpl service;

    @BeforeEach
    void setUp() {
        requirementRepository = mock(CareerListingSkillRequirementRepository.class);
        internshipRepository = mock(InternshipRepository.class);
        jobRepository = mock(JobRepository.class);
        skillRepository = mock(SkillRepository.class);
        service = new CareerListingSkillRequirementServiceImpl(requirementRepository, internshipRepository,
                jobRepository, skillRepository);
    }

    @Test
    void addsAnInternshipRequirementToItsInternshipForeignKey() {
        Internship internship = mock(Internship.class);
        Skill skill = skill();
        when(internshipRepository.findById(4L)).thenReturn(Optional.of(internship));
        when(skillRepository.findById(8L)).thenReturn(Optional.of(skill));
        when(requirementRepository.existsByInternshipIdAndSkillId(4L, 8L)).thenReturn(false);
        when(requirementRepository.save(any(CareerListingSkillRequirement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.add(OpportunityType.INTERNSHIP, 4L, request());

        assertEquals(OpportunityType.INTERNSHIP, result.getListingType());
        verify(requirementRepository).save(any(CareerListingSkillRequirement.class));
    }

    @Test
    void rejectsUpdatingRequirementFromAnotherListing() {
        when(requirementRepository.findByIdAndJobId(3L, 10L)).thenReturn(Optional.empty());
        CareerSkillRequirementUpdateRequest request = new CareerSkillRequirementUpdateRequest();
        request.setRequiredLevel(ProficiencyLevel.EXPERT);

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(OpportunityType.JOB, 10L, 3L, request));
        verify(requirementRepository, never()).save(any());
    }

    @Test
    void updatesAndDeletesAJobRequirementOnlyWithinItsParentListing() {
        Job job = mock(Job.class);
        when(job.getId()).thenReturn(12L);
        CareerListingSkillRequirement requirement = new CareerListingSkillRequirement(null, job, skill(),
                ProficiencyLevel.INTERMEDIATE);
        when(requirementRepository.findByIdAndJobId(3L, 12L)).thenReturn(Optional.of(requirement));
        when(requirementRepository.save(requirement)).thenReturn(requirement);
        CareerSkillRequirementUpdateRequest request = new CareerSkillRequirementUpdateRequest();
        request.setRequiredLevel(ProficiencyLevel.EXPERT);

        var updated = service.update(OpportunityType.JOB, 12L, 3L, request);
        service.delete(OpportunityType.JOB, 12L, 3L);

        assertEquals(ProficiencyLevel.EXPERT, updated.getRequiredLevel());
        verify(requirementRepository).delete(requirement);
    }

    private Skill skill() {
        Skill skill = mock(Skill.class);
        when(skill.getId()).thenReturn(8L);
        when(skill.getName()).thenReturn("Java");
        return skill;
    }

    private SkillRequirementRequest request() {
        SkillRequirementRequest request = new SkillRequirementRequest();
        request.setSkillId(8L);
        request.setRequiredLevel(ProficiencyLevel.INTERMEDIATE);
        return request;
    }
}