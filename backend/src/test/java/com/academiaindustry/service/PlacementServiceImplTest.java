package com.academiaindustry.service;

import com.academiaindustry.dto.PlacementRequest;
import com.academiaindustry.entity.Placement;
import com.academiaindustry.entity.PlacementStatus;
import com.academiaindustry.entity.Application;
import com.academiaindustry.entity.Student;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.repository.ApplicationRepository;
import com.academiaindustry.repository.PlacementRepository;
import com.academiaindustry.service.impl.PlacementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlacementServiceImplTest {

    private PlacementRepository placementRepository;
    private PlacementServiceImpl service;

    @BeforeEach
    void setUp() {
        placementRepository = mock(PlacementRepository.class);
        service = new PlacementServiceImpl(placementRepository, mock(ApplicationRepository.class));
    }

    @Test
    void permitsShortlistedToInterviewTransition() {
        Placement placement = mock(Placement.class);
        Application application = mock(Application.class);
        Student student = mock(Student.class);
        when(placementRepository.findById(1L)).thenReturn(Optional.of(placement));
        when(placement.getStatus()).thenReturn(PlacementStatus.SHORTLISTED);
        when(placement.getApplication()).thenReturn(application);
        when(application.getId()).thenReturn(1L);
        when(application.getStudent()).thenReturn(student);
        when(student.getId()).thenReturn(1L);
        when(placement.getId()).thenReturn(1L);
        when(placementRepository.save(any(Placement.class))).thenReturn(placement);

        PlacementRequest request = request(PlacementStatus.INTERVIEW);

        service.update(1L, request);
    }

    @Test
    void rejectsSelectedToAppliedLikeInvalidTransition() {
        Placement placement = mock(Placement.class);
        when(placementRepository.findById(1L)).thenReturn(Optional.of(placement));
        when(placement.getStatus()).thenReturn(PlacementStatus.SELECTED);

        assertThrows(BusinessRuleException.class,
                () -> service.update(1L, request(PlacementStatus.REJECTED)));
    }

    private PlacementRequest request(PlacementStatus status) {
        PlacementRequest request = new PlacementRequest();
        request.setApplicationId(1L);
        request.setStatus(status);
        return request;
    }
}
