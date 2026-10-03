package com.academiaindustry.service.impl;

import com.academiaindustry.dto.JobRequest;
import com.academiaindustry.dto.JobResponse;
import com.academiaindustry.entity.Job;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.JobRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    public JobResponse create(JobRequest request) {
        User industry = findUser(request.getIndustryId());
        Job job = new Job(industry, request.getTitle(), request.getDescription(), request.getLocation(),
                request.getEmploymentType(), request.getMinimumCgpa(), request.getApplicationDeadline(),
                request.getStatus());
        return toResponse(jobRepository.save(job));
    }

    @Override
    public JobResponse getById(Long id) {
        return toResponse(findJob(id));
    }

    @Override
    public List<JobResponse> getAll() {
        return jobRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public JobResponse update(Long id, JobRequest request) {
        Job job = findJob(id);
        job.setIndustry(findUser(request.getIndustryId()));
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setMinimumCgpa(request.getMinimumCgpa());
        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setStatus(request.getStatus());
        return toResponse(jobRepository.save(job));
    }

    @Override
    public void delete(Long id) {
        jobRepository.delete(findJob(id));
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private JobResponse toResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setIndustryId(job.getIndustry().getId());
        response.setCompanyName(companyName(job.getIndustry()));
        response.setCompanyWebsite(job.getIndustry().getWebsite());
        response.setIndustrySector(job.getIndustry().getIndustrySector());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setEmploymentType(job.getEmploymentType());
        response.setMinimumCgpa(job.getMinimumCgpa());
        response.setApplicationDeadline(job.getApplicationDeadline());
        response.setStatus(job.getStatus());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }

    private String companyName(User industry) {
        return industry.getCompanyName() == null || industry.getCompanyName().isBlank()
                ? industry.getName() : industry.getCompanyName();
    }
}