package com.academiaindustry.security;

import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.PlacementRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.entity.OpportunityType;
import org.springframework.stereotype.Component;

@Component("ownershipSecurity")
public class OwnershipSecurity {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InternshipRepository internshipRepository;
    private final JobRepository jobRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final PlacementRepository placementRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final InstitutionRepository institutionRepository;

    public OwnershipSecurity(UserRepository userRepository, StudentRepository studentRepository,
                             InternshipRepository internshipRepository, JobRepository jobRepository,
                             OpportunityRepository opportunityRepository, ApplicationRepository applicationRepository,
                             PlacementRepository placementRepository, StudentSkillRepository studentSkillRepository,
                             InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.internshipRepository = internshipRepository;
        this.jobRepository = jobRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.placementRepository = placementRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.institutionRepository = institutionRepository;
    }

    public boolean isUser(String email, Long userId) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    public boolean isStudentOwner(Long studentId, String email) {
        return studentRepository.existsByIdAndUserEmailIgnoreCase(studentId, email);
    }

    public boolean isStudentInstitutionOwner(Long studentId, String email) {
        return studentRepository.existsByIdAndInstitutionAccountEmailIgnoreCase(studentId, email);
    }

    public boolean isFacultyForStudent(Long studentId, String email) {
        return studentRepository.existsByIdAndFacultyInstitutionEmailIgnoreCase(studentId, email);
    }

    public boolean isInstitutionOwner(String email, Long institutionId) {
        return institutionRepository.existsByIdAndAccountEmailIgnoreCase(institutionId, email);
    }

    public boolean isInternshipOwner(Long internshipId, String email) {
        return internshipRepository.existsByIdAndIndustryEmailIgnoreCase(internshipId, email);
    }

    public boolean isJobOwner(Long jobId, String email) {
        return jobRepository.existsByIdAndIndustryEmailIgnoreCase(jobId, email);
    }

    public boolean isOpportunityOwner(Long opportunityId, String email) {
        return opportunityRepository.existsByIdAndIndustryEmailIgnoreCase(opportunityId, email);
    }

    public boolean isCareerListingOwner(OpportunityType type, Long listingId, String email) {
        if (type == null || listingId == null || email == null) return false;
        return switch (type) {
            case INTERNSHIP -> internshipRepository.existsByIdAndIndustryEmailIgnoreCase(listingId, email);
            case JOB -> jobRepository.existsByIdAndIndustryEmailIgnoreCase(listingId, email);
            default -> false;
        };
    }

    public boolean isApplicationAccessible(Long applicationId, String email) {
        return applicationRepository.findById(applicationId)
                .map(application -> studentOwns(application.getStudent().getId(), email)
                        || opportunityOwnedBy(application.getOpportunityType(), application.getOpportunityId(), email))
                .orElse(false);
    }

        public boolean isApplicationOpportunityOwner(Long applicationId, String email) {
        return applicationRepository.findById(applicationId)
            .map(application -> opportunityOwnedBy(application.getOpportunityType(),
                application.getOpportunityId(), email))
            .orElse(false);
        }

    public boolean isPlacementAccessible(Long placementId, String email) {
        return placementRepository.findById(placementId)
                .map(placement -> isApplicationAccessible(placement.getApplication().getId(), email))
                .orElse(false);
    }

    public boolean isStudentSkillOwner(Long studentSkillId, String email) {
        return studentSkillRepository.findById(studentSkillId)
                .map(studentSkill -> studentOwns(studentSkill.getStudent().getId(), email))
                .orElse(false);
    }

    private boolean studentOwns(Long studentId, String email) {
        return studentRepository.existsByIdAndUserEmailIgnoreCase(studentId, email);
    }

    private boolean opportunityOwnedBy(OpportunityType type, Long opportunityId, String email) {
        return switch (type) {
            case PROJECT, APPRENTICESHIP, PROGRAM -> opportunityRepository
                    .existsByIdAndIndustryEmailIgnoreCase(opportunityId, email);
            case INTERNSHIP -> internshipRepository.existsByIdAndIndustryEmailIgnoreCase(opportunityId, email);
            case JOB -> jobRepository.existsByIdAndIndustryEmailIgnoreCase(opportunityId, email);
        };
    }
}