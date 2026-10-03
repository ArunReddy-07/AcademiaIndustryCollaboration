package com.academiaindustry.service;

import com.academiaindustry.dto.SkillAssessmentRequest;
import com.academiaindustry.dto.SkillAssessmentResponse;

import java.util.List;

public interface SkillAssessmentService {

    SkillAssessmentResponse submit(SkillAssessmentRequest request);

    List<SkillAssessmentResponse> getMine(String email);
}