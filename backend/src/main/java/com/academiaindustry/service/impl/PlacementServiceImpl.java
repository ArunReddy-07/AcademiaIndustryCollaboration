package com.academiaindustry.service.impl;

import com.academiaindustry.dto.PlacementRequest;
import com.academiaindustry.dto.PlacementResponse;
import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.Placement;
import com.academiaindustry.entity.PlacementStatus;
import com.academiaindustry.entity.ApplicationStatus;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.exception.DuplicateResourceException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.PlacementRepository;
import com.academiaindustry.service.PlacementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlacementServiceImpl implements PlacementService {

    private final PlacementRepository placementRepository;
    private final ApplicationRepository applicationRepository;

    public PlacementServiceImpl(PlacementRepository placementRepository, ApplicationRepository applicationRepository) {
        this.placementRepository = placementRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public PlacementResponse create(PlacementRequest request) {
        Application application = findApplication(request.getApplicationId());
        validateApplicationStatus(application);
        validateInitialStatus(request.getStatus());
        if (placementRepository.findByApplicationId(request.getApplicationId()).isPresent()) {
            throw new DuplicateResourceException("A placement record already exists for this application.");
        }
        return toResponse(placementRepository.save(new Placement(application, request.getStatus(),
                request.getInterviewDate(), request.getNotes())));
    }

    @Override
    public PlacementResponse getById(Long id) {
        return toResponse(findPlacement(id));
    }

    @Override
    public List<PlacementResponse> getAll() {
        return placementRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PlacementResponse> getMine(String email) {
        return placementRepository.findAll().stream()
                .filter(placement -> placement.getApplication().getStudent().getUser().getEmail().equalsIgnoreCase(email))
                .map(this::toResponse).toList();
    }

    @Override
    public PlacementResponse update(Long id, PlacementRequest request) {
        Placement placement = findPlacement(id);
        validateTransition(placement.getStatus(), request.getStatus());
        placement.setStatus(request.getStatus());
        placement.setInterviewDate(request.getInterviewDate());
        placement.setNotes(request.getNotes());
        return toResponse(placementRepository.save(placement));
    }

    private Application findApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    private void validateApplicationStatus(Application application) {
        if (application.getStatus() != ApplicationStatus.SHORTLISTED
                && application.getStatus() != ApplicationStatus.INTERVIEW
                && application.getStatus() != ApplicationStatus.SELECTED) {
            throw new BusinessRuleException("A placement requires a shortlisted, interview, or selected application.");
        }
    }

    private void validateInitialStatus(PlacementStatus status) {
        if (status != PlacementStatus.SHORTLISTED && status != PlacementStatus.INTERVIEW
                && status != PlacementStatus.SELECTED) {
            throw new BusinessRuleException("A placement must start in SHORTLISTED, INTERVIEW, or SELECTED status.");
        }
    }

    private void validateTransition(PlacementStatus current, PlacementStatus next) {
        if (current == next) {
            return;
        }
        boolean valid = switch (current) {
            case SHORTLISTED -> next == PlacementStatus.INTERVIEW || next == PlacementStatus.REJECTED;
            case INTERVIEW -> next == PlacementStatus.SELECTED || next == PlacementStatus.REJECTED;
            case SELECTED -> next == PlacementStatus.COMPLETED;
            case REJECTED, COMPLETED -> false;
        };
        if (!valid) {
            throw new BusinessRuleException("Invalid placement status transition: " + current + " -> " + next);
        }
    }

    private Placement findPlacement(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found with id: " + id));
    }

    private PlacementResponse toResponse(Placement placement) {
        PlacementResponse response = new PlacementResponse();
        response.setId(placement.getId());
        response.setApplicationId(placement.getApplication().getId());
        response.setStudentId(placement.getApplication().getStudent().getId());
        response.setStatus(placement.getStatus());
        response.setInterviewDate(placement.getInterviewDate());
        response.setNotes(placement.getNotes());
        response.setCreatedAt(placement.getCreatedAt());
        response.setUpdatedAt(placement.getUpdatedAt());
        return response;
    }
}