package com.academiaindustry.service.impl;

import com.academiaindustry.dto.InternshipRequest;
import com.academiaindustry.dto.InternshipResponse;
import com.academiaindustry.entity.Internship;
import com.academiaindustry.entity.User;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.InternshipRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.InternshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    public InternshipServiceImpl(InternshipRepository internshipRepository, UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public InternshipResponse create(InternshipRequest request) {
        User industry = findUser(request.getIndustryId());
        Internship internship = new Internship(industry, request.getTitle(), request.getDescription(),
                request.getLocation(), request.getDuration(), request.getStipend(),
                request.getApplicationDeadline(), request.getStatus());
        return toResponse(internshipRepository.save(internship));
    }

    @Override
    public InternshipResponse getById(Long id) {
        return toResponse(findInternship(id));
    }

    @Override
    public List<InternshipResponse> getAll() {
        return internshipRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public InternshipResponse update(Long id, InternshipRequest request) {
        Internship internship = findInternship(id);
        internship.setIndustry(findUser(request.getIndustryId()));
        internship.setTitle(request.getTitle());
        internship.setDescription(request.getDescription());
        internship.setLocation(request.getLocation());
        internship.setDuration(request.getDuration());
        internship.setStipend(request.getStipend());
        internship.setApplicationDeadline(request.getApplicationDeadline());
        internship.setStatus(request.getStatus());
        return toResponse(internshipRepository.save(internship));
    }

    @Override
    public void delete(Long id) {
        internshipRepository.delete(findInternship(id));
    }

    private Internship findInternship(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private InternshipResponse toResponse(Internship internship) {
        InternshipResponse response = new InternshipResponse();
        response.setId(internship.getId());
        response.setIndustryId(internship.getIndustry().getId());
        response.setCompanyName(companyName(internship.getIndustry()));
        response.setCompanyWebsite(internship.getIndustry().getWebsite());
        response.setIndustrySector(internship.getIndustry().getIndustrySector());
        response.setTitle(internship.getTitle());
        response.setDescription(internship.getDescription());
        response.setLocation(internship.getLocation());
        response.setDuration(internship.getDuration());
        response.setStipend(internship.getStipend());
        response.setApplicationDeadline(internship.getApplicationDeadline());
        response.setStatus(internship.getStatus());
        response.setCreatedAt(internship.getCreatedAt());
        response.setUpdatedAt(internship.getUpdatedAt());
        return response;
    }

    private String companyName(User industry) {
        return industry.getCompanyName() == null || industry.getCompanyName().isBlank()
                ? industry.getName() : industry.getCompanyName();
    }
}