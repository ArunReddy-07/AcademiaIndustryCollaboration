package com.academiaindustry.service;

import com.academiaindustry.dto.ResumeAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeAnalysisService {

    ResumeAnalysisResponse analyze(String studentEmail, MultipartFile file);

    ResumeAnalysisResponse getForStudent(String studentEmail);
}
