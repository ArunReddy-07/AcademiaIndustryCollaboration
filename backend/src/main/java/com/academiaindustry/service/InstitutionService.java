package com.academiaindustry.service;

import com.academiaindustry.dto.InstitutionRequest;
import com.academiaindustry.dto.InstitutionResponse;

import java.util.List;

public interface InstitutionService {

    InstitutionResponse create(InstitutionRequest request);

    InstitutionResponse findOrCreateByName(String name);

    InstitutionResponse getForAccount(String email);

    InstitutionResponse updateForAccount(String email, InstitutionRequest request);

    InstitutionResponse getById(Long id);

    List<InstitutionResponse> getAll();

    InstitutionResponse update(Long id, InstitutionRequest request);

    void delete(Long id);
}