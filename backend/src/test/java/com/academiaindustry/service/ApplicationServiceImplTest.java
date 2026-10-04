package com.academiaindustry.service;

import com.academiaindustry.dto.ApplicationRequest;
import com.academiaindustry.dto.ApplicationResponse;
import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.entity.OpportunityStatus;
import com.academiaindustry.entity.OpportunityType;
import com.academiaindustry.entity.PortfolioItemType;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentPortfolioItem;
import com.academiaindustry.entity.User;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.StudentPortfolioItemRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationServiceImplTest {

    private ApplicationRepository applicationRepository;
    private StudentRepository studentRepository;
    private OpportunityRepository opportunityRepository;
    private StudentPortfolioItemRepository portfolioItemRepository;
    private ApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        studentRepository = mock(StudentRepository.class);
        opportunityRepository = mock(OpportunityRepository.class);
        portfolioItemRepository = mock(StudentPortfolioItemRepository.class);
        service = new ApplicationServiceImpl(applicationRepository, studentRepository, opportunityRepository,
                mock(InternshipRepository.class), mock(JobRepository.class), portfolioItemRepository);
    }

    @Test
    void applicationResponseContainsOnlyEvidenceSelectedForCompanySharing() {
        Student student = mock(Student.class);
        User user = mock(User.class);
        Institution institution = mock(Institution.class);
        Opportunity opportunity = mock(Opportunity.class);
        StudentPortfolioItem selected = portfolioItem(11L, "Selected certificate");

        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(student.getId()).thenReturn(3L);
        when(student.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("Student");
        when(user.getEmail()).thenReturn("student@example.com");
        when(student.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("University");
        when(opportunityRepository.findById(7L)).thenReturn(Optional.of(opportunity));
        when(opportunity.getStatus()).thenReturn(OpportunityStatus.OPEN);
        when(opportunity.getApplicationDeadline()).thenReturn(LocalDate.now().plusDays(1));
        when(portfolioItemRepository.findByIdInAndStudentIdAndTypeIn(
                List.of(11L), 3L, List.of(PortfolioItemType.CERTIFICATION, PortfolioItemType.PROJECT)))
                .thenReturn(List.of(selected));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequest request = new ApplicationRequest();
        request.setStudentId(3L);
        request.setOpportunityType(OpportunityType.PROJECT);
        request.setOpportunityId(7L);
        request.setSharedPortfolioItemIds(List.of(11L));

        ApplicationResponse response = service.create(request);

        assertEquals(List.of(11L), response.getSharedPortfolioItems().stream()
                .map(item -> item.getId()).toList());
        assertEquals(List.of("Selected certificate"), response.getSharedPortfolioItems().stream()
                .map(item -> item.getTitle()).toList());
        assertEquals(1, response.getSharedPortfolioItems().size());
        verify(portfolioItemRepository).findByIdInAndStudentIdAndTypeIn(
                List.of(11L), 3L, List.of(PortfolioItemType.CERTIFICATION, PortfolioItemType.PROJECT));
    }

    @Test
    void industryApplicantsResponseIncludesPersistedSharedEvidence() {
        Application application = mock(Application.class);
        Student student = mock(Student.class);
        User user = mock(User.class);
        Institution institution = mock(Institution.class);
        StudentPortfolioItem selected = portfolioItem(11L, "Selected certificate");

        when(applicationRepository.findByOpportunityTypeAndOpportunityId(
                OpportunityType.PROJECT, 7L)).thenReturn(List.of(application));
        when(application.getId()).thenReturn(21L);
        when(application.getStudent()).thenReturn(student);
        when(application.getOpportunityType()).thenReturn(OpportunityType.PROJECT);
        when(application.getOpportunityId()).thenReturn(7L);
        when(application.getStatus()).thenReturn(ApplicationStatus.APPLIED);
        when(application.getAppliedAt()).thenReturn(LocalDateTime.now());
        when(application.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(application.getSharedPortfolioItems()).thenReturn(Set.of(selected));
        when(student.getId()).thenReturn(3L);
        when(student.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("Student");
        when(user.getEmail()).thenReturn("student@example.com");
        when(student.getInstitution()).thenReturn(institution);
        when(institution.getName()).thenReturn("University");

        List<ApplicationResponse> responses = service.getForOpportunity(
                "company@example.com", OpportunityType.PROJECT, 7L, true);

        assertEquals(List.of(11L), responses.get(0).getSharedPortfolioItems().stream()
                .map(item -> item.getId()).toList());
        assertEquals(List.of("Selected certificate"), responses.get(0).getSharedPortfolioItems().stream()
                .map(item -> item.getTitle()).toList());
    }

    private StudentPortfolioItem portfolioItem(Long id, String title) {
        StudentPortfolioItem item = mock(StudentPortfolioItem.class);
        when(item.getId()).thenReturn(id);
        when(item.getType()).thenReturn(PortfolioItemType.CERTIFICATION);
        when(item.getTitle()).thenReturn(title);
        return item;
    }
}
