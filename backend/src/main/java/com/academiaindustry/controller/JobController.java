package com.academiaindustry.controller;

import com.academiaindustry.dto.JobRequest;
import com.academiaindustry.dto.JobResponse;
import com.academiaindustry.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobResponse> getAll() {
        return jobService.getAll();
    }

    @GetMapping("/{id}")
    public JobResponse getById(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @PostMapping
        @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isUser(authentication.name, #request.industryId))")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isJobOwner(#id, authentication.name))")
    public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('INDUSTRY') and @ownershipSecurity.isJobOwner(#id, authentication.name))")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}