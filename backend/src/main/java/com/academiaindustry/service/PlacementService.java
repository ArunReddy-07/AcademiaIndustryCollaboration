package com.academiaindustry.service;

import com.academiaindustry.dto.PlacementRequest;
import com.academiaindustry.dto.PlacementResponse;

import java.util.List;

public interface PlacementService {
    PlacementResponse create(PlacementRequest request);
    PlacementResponse getById(Long id);
    List<PlacementResponse> getAll();
    List<PlacementResponse> getMine(String email);
    PlacementResponse update(Long id, PlacementRequest request);
}