package com.academiaindustry.service;

import com.academiaindustry.dto.JobRequest;
import com.academiaindustry.dto.JobResponse;

import java.util.List;

public interface JobService {

    JobResponse create(JobRequest request);

    JobResponse getById(Long id);

    List<JobResponse> getAll();

    JobResponse update(Long id, JobRequest request);

    void delete(Long id);
}