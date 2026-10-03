package com.academiaindustry.service;

import com.academiaindustry.dto.InternshipRequest;
import com.academiaindustry.dto.InternshipResponse;

import java.util.List;

public interface InternshipService {

    InternshipResponse create(InternshipRequest request);

    InternshipResponse getById(Long id);

    List<InternshipResponse> getAll();

    InternshipResponse update(Long id, InternshipRequest request);

    void delete(Long id);
}