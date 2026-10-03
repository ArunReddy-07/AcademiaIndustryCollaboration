package com.academiaindustry.service.impl;

import com.academiaindustry.dto.InstitutionRequest;
import com.academiaindustry.dto.InstitutionResponse;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.service.InstitutionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;

    public InstitutionServiceImpl(InstitutionRepository institutionRepository, StudentRepository studentRepository) {
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public InstitutionResponse create(InstitutionRequest request) {
        Institution institution = new Institution(request.getName(), request.getWebsite(), request.getLocation(),
                request.getDescription(), null);
        return toResponse(institutionRepository.save(institution));
    }

    @Override
    public InstitutionResponse findOrCreateByName(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank()) {
            throw new BusinessRuleException("Institution name is required.");
        }
        return institutionRepository.findByNameIgnoreCase(normalizedName)
                .map(this::toResponse)
                .orElseGet(() -> toResponse(institutionRepository.save(new Institution(normalizedName))));
    }

    @Override
    public InstitutionResponse getForAccount(String email) {
        return institutionRepository.findByAccountEmailIgnoreCase(email).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Institution profile not found."));
    }

    @Override
    public InstitutionResponse updateForAccount(String email, InstitutionRequest request) {
        Institution institution = institutionRepository.findByAccountEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Institution profile not found."));
        updateProfile(institution, request);
        return toResponse(institutionRepository.save(institution));
    }

    @Override
    public InstitutionResponse getById(Long id) {
        return toResponse(findInstitution(id));
    }

    @Override
    public List<InstitutionResponse> getAll() {
        return institutionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public InstitutionResponse update(Long id, InstitutionRequest request) {
        Institution institution = findInstitution(id);
        updateProfile(institution, request);
        return toResponse(institutionRepository.save(institution));
    }

    private void updateProfile(Institution institution, InstitutionRequest request) {
        institution.setName(request.getName());
        institution.setWebsite(request.getWebsite());
        institution.setLocation(request.getLocation());
        institution.setDescription(request.getDescription());
    }

    @Override
    public void delete(Long id) {
        Institution institution = findInstitution(id);
        if (studentRepository.existsByInstitutionId(id)) {
            throw new BusinessRuleException("An institution with student profiles cannot be deleted.");
        }
        if (institution.getAccount() != null) {
            throw new BusinessRuleException("An institution account must be reassigned before the institution is deleted.");
        }
        institutionRepository.delete(institution);
    }

    private Institution findInstitution(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with id: " + id));
    }

    private InstitutionResponse toResponse(Institution institution) {
        InstitutionResponse response = new InstitutionResponse();
        response.setId(institution.getId());
        response.setName(institution.getName());
        response.setWebsite(institution.getWebsite());
        response.setLocation(institution.getLocation());
        response.setDescription(institution.getDescription());
        return response;
    }
}