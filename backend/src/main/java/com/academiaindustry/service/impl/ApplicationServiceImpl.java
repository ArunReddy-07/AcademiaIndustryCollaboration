package com.academiaindustry.service.impl;

import com.academiaindustry.dto.ApplicationRequest;
import com.academiaindustry.dto.ApplicationResponse;
import com.academiaindustry.dto.StudentPortfolioItemResponse;
import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.PortfolioItemType;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentPortfolioItem;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentPortfolioItemRepository;
import com.academiaindustry.service.ApplicationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final InternshipRepository internshipRepository;
    private final JobRepository jobRepository;
    private final StudentPortfolioItemRepository portfolioItemRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, StudentRepository studentRepository,
                                  OpportunityRepository opportunityRepository,
                                  InternshipRepository internshipRepository, JobRepository jobRepository,
                                  StudentPortfolioItemRepository portfolioItemRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.internshipRepository = internshipRepository;
        this.jobRepository = jobRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @Override
    public ApplicationResponse create(ApplicationRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));
        validateOpportunity(request.getOpportunityType(), request.getOpportunityId());
        if (applicationRepository.existsByStudentIdAndOpportunityTypeAndOpportunityId(
                request.getStudentId(), request.getOpportunityType(), request.getOpportunityId())) {
            throw new DuplicateResourceException("Student has already applied to this opportunity.");
        }
        Application application = new Application(student, request.getOpportunityType(), request.getOpportunityId(),
                ApplicationStatus.APPLIED);
        List<Long> selectedItemIds = request.getSharedPortfolioItemIds();
        if (!selectedItemIds.isEmpty()) {
            List<StudentPortfolioItem> selectedItems = portfolioItemRepository.findByIdInAndStudentIdAndTypeIn(
                    selectedItemIds, student.getId(), List.of(PortfolioItemType.CERTIFICATION, PortfolioItemType.PROJECT));
            if (selectedItems.size() != new LinkedHashSet<>(selectedItemIds).size()) {
                throw new IllegalArgumentException("Only your own certificates and projects can be shared with an application.");
            }
            application.setSharedPortfolioItems(new LinkedHashSet<>(selectedItems));
        }
        return toResponse(applicationRepository.save(application));
    }

    @Override
    public ApplicationResponse getById(Long id) {
        return toResponse(findApplication(id));
    }

    @Override
    public List<ApplicationResponse> getAll() {
        return applicationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<ApplicationResponse> getMine(String email) {
        return applicationRepository.findByStudentUserEmailIgnoreCase(email)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<ApplicationResponse> getForOpportunity(String email, OpportunityType type, Long opportunityId,
                                                       boolean administrator) {
        if (!administrator && !isOpportunityOwner(email, type, opportunityId)) {
            throw new AccessDeniedException("You do not own this opportunity.");
        }
        return applicationRepository.findByOpportunityTypeAndOpportunityId(type, opportunityId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public ApplicationResponse updateStatus(Long id, ApplicationStatus status, String email, boolean administrator) {
        Application application = findApplication(id);
        if (!administrator && !isOpportunityOwner(email, application.getOpportunityType(), application.getOpportunityId())) {
            throw new AccessDeniedException("You do not own this opportunity.");
        }
        application.setStatus(status);
        return toResponse(applicationRepository.save(application));
    }

    @Override
    public ApplicationResponse updateSharedPortfolioItems(Long id, List<Long> itemIds, String email) {
        Application application = findApplication(id);
        if (!application.getStudent().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You can only update evidence on your own application.");
        }
        List<Long> selectedItemIds = itemIds == null ? List.of() : itemIds;
        List<StudentPortfolioItem> selectedItems = selectedItemIds.isEmpty()
                ? List.of()
                : portfolioItemRepository.findByIdInAndStudentIdAndTypeIn(
                        selectedItemIds, application.getStudent().getId(),
                        List.of(PortfolioItemType.CERTIFICATION, PortfolioItemType.PROJECT));
        if (selectedItems.size() != new LinkedHashSet<>(selectedItemIds).size()) {
            throw new IllegalArgumentException("Only your own certificates and projects can be shared with an application.");
        }
        application.setSharedPortfolioItems(new LinkedHashSet<>(selectedItems));
        return toResponse(applicationRepository.save(application));
    }

    private void validateOpportunity(OpportunityType type, Long id) {
        boolean eligible = switch (type) {
            case PROJECT, APPRENTICESHIP, PROGRAM -> opportunityRepository.findById(id)
                .map(opportunity -> opportunity.getStatus() == com.academiaindustry.entity.OpportunityStatus.OPEN
                    && !opportunity.getApplicationDeadline().isBefore(LocalDate.now()))
                .orElse(false);
            case INTERNSHIP -> internshipRepository.findById(id)
                .map(internship -> internship.getStatus() == com.academiaindustry.entity.OpportunityStatus.OPEN
                    && !internship.getApplicationDeadline().isBefore(LocalDate.now()))
                .orElse(false);
            case JOB -> jobRepository.findById(id)
                .map(job -> job.getStatus() == com.academiaindustry.entity.OpportunityStatus.OPEN
                    && !job.getApplicationDeadline().isBefore(LocalDate.now()))
                .orElse(false);
        };
        if (!eligible) {
            throw new IllegalArgumentException("Opportunity is not open or its application deadline has passed.");
        }
    }

    private boolean isOpportunityOwner(String email, OpportunityType type, Long id) {
        return switch (type) {
            case PROJECT, APPRENTICESHIP, PROGRAM -> opportunityRepository.existsByIdAndIndustryEmailIgnoreCase(id, email);
            case INTERNSHIP -> internshipRepository.existsByIdAndIndustryEmailIgnoreCase(id, email);
            case JOB -> jobRepository.existsByIdAndIndustryEmailIgnoreCase(id, email);
        };
    }

    private Application findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    private ApplicationResponse toResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setStudentId(application.getStudent().getId());
        response.setStudentName(application.getStudent().getUser().getName());
        response.setStudentEmail(application.getStudent().getUser().getEmail());
        response.setInstitutionName(application.getStudent().getInstitution().getName());
        response.setBranch(application.getStudent().getBranch());
        response.setGraduationYear(application.getStudent().getGraduationYear());
        response.setCgpa(application.getStudent().getCgpa());
        response.setOpportunityType(application.getOpportunityType());
        response.setOpportunityId(application.getOpportunityId());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setSharedPortfolioItems(application.getSharedPortfolioItems().stream()
                .map(ApplicationServiceImpl::toPortfolioResponse).toList());
        return response;
    }

    private static StudentPortfolioItemResponse toPortfolioResponse(StudentPortfolioItem item) {
        StudentPortfolioItemResponse response = new StudentPortfolioItemResponse();
        response.setId(item.getId());
        response.setType(item.getType());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setOrganization(item.getOrganization());
        response.setReferenceUrl(item.getReferenceUrl());
        response.setCompletedOn(item.getCompletedOn());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}