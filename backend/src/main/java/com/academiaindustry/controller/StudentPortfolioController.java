package com.academiaindustry.controller;

import com.academiaindustry.dto.StudentPortfolioItemRequest;
import com.academiaindustry.dto.StudentPortfolioItemResponse;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentPortfolioItem;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.StudentPortfolioItemRepository;
import com.academiaindustry.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/students/me/portfolio-items")
@PreAuthorize("hasRole('STUDENT')")
public class StudentPortfolioController {

    private final StudentPortfolioItemRepository portfolioRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;

    public StudentPortfolioController(StudentPortfolioItemRepository portfolioRepository,
                                      StudentRepository studentRepository,
                                      ApplicationRepository applicationRepository) {
        this.portfolioRepository = portfolioRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    public List<StudentPortfolioItemResponse> getMine(Authentication authentication) {
        return portfolioRepository.findByStudentUserEmailIgnoreCaseOrderByCreatedAtDesc(authentication.getName())
                .stream().map(StudentPortfolioController::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentPortfolioItemResponse create(Authentication authentication,
                                               @Valid @RequestBody StudentPortfolioItemRequest request) {
        Student student = studentRepository.findByUserEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));
        StudentPortfolioItem item = new StudentPortfolioItem(student, request.getType(), request.getTitle(),
                request.getDescription(), request.getOrganization(), request.getReferenceUrl(), request.getCompletedOn());
        return toResponse(portfolioRepository.save(item));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(Authentication authentication, @PathVariable Long id) {
        StudentPortfolioItem item = portfolioRepository.findByIdAndStudentUserEmailIgnoreCase(id, authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item not found."));
        applicationRepository.findBySharedPortfolioItems_Id(id).forEach(application -> {
            application.getSharedPortfolioItems().remove(item);
            applicationRepository.save(application);
        });
        portfolioRepository.delete(item);
    }

    private static StudentPortfolioItemResponse toResponse(StudentPortfolioItem item) {
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
