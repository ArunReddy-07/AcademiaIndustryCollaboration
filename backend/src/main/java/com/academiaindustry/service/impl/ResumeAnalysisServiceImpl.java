package com.academiaindustry.service.impl;

import com.academiaindustry.dto.ResumeAnalysisResponse;
import com.academiaindustry.entity.ResumeAnalysis;
import com.academiaindustry.entity.Skill;
import com.academiaindustry.entity.Student;
import com.academiaindustry.entity.StudentSkill;
import com.academiaindustry.exception.BusinessRuleException;
import com.academiaindustry.exception.ResourceNotFoundException;
import com.academiaindustry.repository.ResumeAnalysisRepository;
import com.academiaindustry.repository.SkillRepository;
import com.academiaindustry.repository.StudentRepository;
import com.academiaindustry.repository.StudentSkillRepository;
import com.academiaindustry.service.ResumeAnalysisService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResumeAnalysisServiceImpl implements ResumeAnalysisService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final List<String> SECTION_HINTS = List.of(
            "education", "experience", "projects", "skills", "certifications", "achievements", "summary",
            "objective", "internships", "work experience"
    );

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final SkillRepository skillRepository;

    public ResumeAnalysisServiceImpl(ResumeAnalysisRepository resumeAnalysisRepository,
                                    StudentRepository studentRepository,
                                    StudentSkillRepository studentSkillRepository,
                                    SkillRepository skillRepository) {
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse analyze(String studentEmail, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Resume file is required.");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()) && !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new BusinessRuleException("Only PDF resumes are supported.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Resume file must be 5 MB or smaller.");
        }

        Student student = studentRepository.findByUserEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found."));

        String extractedText;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new BusinessRuleException("Resume PDF is encrypted and cannot be analyzed.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        } catch (IOException | IllegalArgumentException exception) {
            throw new BusinessRuleException("The uploaded PDF could not be read or appears to be invalid.");
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new BusinessRuleException("No readable text was found in the uploaded PDF.");
        }

        ResumeAnalysisResult result = analyzeText(extractedText, student);
        ResumeAnalysis entity = new ResumeAnalysis(
                student,
                file.getOriginalFilename(),
                extractedText,
                result.toJson(),
                result.overallScore(),
                result.summary()
        );

        ResumeAnalysis saved = resumeAnalysisRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getForStudent(String studentEmail) {
        ResumeAnalysis latest = resumeAnalysisRepository.findTopByStudentUserEmailIgnoreCaseOrderByUpdatedAtDesc(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No resume analysis found for this student."));
        return toResponse(latest);
    }

    private ResumeAnalysisResult analyzeText(String text, Student student) {
        String normalizedText = normalizeText(text);

        List<String> extractedSkills = extractSkills(normalizedText);
        List<String> studentSkillNames = studentSkillRepository.findByStudentId(student.getId()).stream()
                .map(studentSkill -> studentSkill.getSkill().getName())
                .map(this::normalizeSkillToken)
                .distinct()
                .toList();

        Set<String> existingSkillSet = new LinkedHashSet<>(studentSkillNames);
        List<String> skillGapSkills = extractedSkills.stream()
                .filter(skill -> !existingSkillSet.contains(normalizeSkillToken(skill)))
                .distinct()
                .toList();

        int profileCompleteness = calculateProfileCompleteness(normalizedText);
        List<String> missingSections = detectMissingSections(normalizedText);
        List<String> weakSignals = detectWeakContentSignals(normalizedText);
        List<String> strengths = detectStrengthSignals(normalizedText, extractedSkills);

        List<ResumeAnalysisResponse.ResumeCategoryScore> categoryBreakdown = new ArrayList<>();
        categoryBreakdown.add(new ResumeAnalysisResponse.ResumeCategoryScore("Profile completeness", profileCompleteness, 25,
                "How complete the resume is based on section coverage."));
        categoryBreakdown.add(new ResumeAnalysisResponse.ResumeCategoryScore("Skills", Math.min(100, extractedSkills.size() * 12), 30,
                "Quality and breadth of technical and role-relevant skills."));
        categoryBreakdown.add(new ResumeAnalysisResponse.ResumeCategoryScore("Experience", Math.min(100, countKeywordMatches(normalizedText, "experience", "intern", "project", "work") * 20), 25,
                "Evidence of practical experience and applied work."));
        categoryBreakdown.add(new ResumeAnalysisResponse.ResumeCategoryScore("Achievements and impact", Math.min(100, countKeywordMatches(normalizedText, "achievement", "impact", "led", "developed", "optimized") * 18), 20,
                "Presence of measurable outcomes and concrete accomplishments."));

        int overallScore = Math.max(0, Math.min(100, categoryBreakdown.stream()
                .mapToInt(score -> score.getScore())
                .sum() / Math.max(1, categoryBreakdown.size())));

        String summary = buildSummary(overallScore, profileCompleteness, missingSections, extractedSkills, weakSignals);
        return new ResumeAnalysisResult(
                overallScore,
                profileCompleteness,
                missingSections,
                weakSignals,
                strengths,
                extractedSkills,
                skillGapSkills,
                categoryBreakdown,
                summary
        );
    }

    private List<String> extractSkills(String text) {
        List<String> knownSkills = skillRepository.findAll().stream()
                .map(Skill::getName)
                .map(this::normalizeSkillToken)
                .distinct()
                .toList();

        Set<String> extracted = new LinkedHashSet<>();
        for (String token : tokenize(text)) {
            String normalized = normalizeSkillToken(token);
            if (normalized.isEmpty()) continue;
            if (knownSkills.contains(normalized) || knownSkills.contains(token.toLowerCase(Locale.ROOT))) {
                extracted.add(capitalizeSkill(normalized));
            }
        }
        return new ArrayList<>(extracted);
    }

    private List<String> detectMissingSections(String text) {
        List<String> missing = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        for (String section : SECTION_HINTS) {
            if (!lower.contains(section)) {
                missing.add(capitalize(section));
            }
        }
        return missing;
    }

    private List<String> detectWeakContentSignals(String text) {
        List<String> signals = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("hardworking") || lower.contains("team player")) signals.add("Generic phrasing instead of concrete results.");
        if (!lower.contains("java") && !lower.contains("python") && !lower.contains("sql") && !lower.contains("aws") && !lower.contains("spring")) {
            signals.add("Few technical keywords or measurable skill signals detected.");
        }
        if (lower.contains("responsible for") && !lower.contains("improved") && !lower.contains("optimized") && !lower.contains("reduced") && !lower.contains("increased")) {
            signals.add("Responsibilities are listed without measurable outcomes.");
        }
        return signals;
    }

    private List<String> detectStrengthSignals(String text, List<String> extractedSkills) {
        List<String> strengths = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        if (extractedSkills.size() > 0) strengths.add("Relevant skill keywords are present in the resume.");
        if (lower.contains("project") || lower.contains("internship") || lower.contains("experience")) {
            strengths.add("Practical experience signals are visible.");
        }
        if (lower.contains("lead") || lower.contains("developed") || lower.contains("built") || lower.contains("optimized")) {
            strengths.add("The resume includes action-oriented impact language.");
        }
        return strengths;
    }

    private int calculateProfileCompleteness(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        int found = 0;
        for (String section : SECTION_HINTS) {
            if (lower.contains(section)) {
                found++;
            }
        }
        return Math.min(100, Math.round((found / (float) SECTION_HINTS.size()) * 100));
    }

    private int countKeywordMatches(String text, String... keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                count++;
            }
        }
        return count;
    }

    private String buildSummary(int score, int profileCompleteness, List<String> missingSections,
                                List<String> extractedSkills, List<String> weakSignals) {
        StringBuilder summary = new StringBuilder();
        summary.append("This resume scores ").append(score).append("/100 with ")
                .append(profileCompleteness).append("% section coverage.");
        if (!missingSections.isEmpty()) {
            summary.append(" Gaps include: ").append(String.join(", ", missingSections)).append(".");
        }
        if (!extractedSkills.isEmpty()) {
            summary.append(" Stronger keywords detected include: ").append(String.join(", ", extractedSkills.stream().limit(5).toList())).append(".");
        }
        if (!weakSignals.isEmpty()) {
            summary.append(" Consider improving: ").append(String.join("; ", weakSignals)).append(".");
        }
        return summary.toString();
    }

    private static String normalizeText(String text) {
        return text.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private static List<String> tokenize(String text) {
        return Arrays.stream(text.split("[^a-zA-Z0-9#+./-]+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    private String normalizeSkillToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private String capitalizeSkill(String value) {
        return Arrays.stream(value.split("\\s+"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private ResumeAnalysisResponse toResponse(ResumeAnalysis entity) {
        ResumeAnalysisResult result = parseAnalysisJson(entity.getAnalysisJson());
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setId(entity.getId());
        response.setFileName(entity.getFileName());
        response.setUploadedAt(entity.getCreatedAt());
        response.setOverallScore(entity.getOverallScore());
        response.setSummary(entity.getSummary());
        response.setProfileCompleteness(result.profileCompleteness());
        response.setMissingSections(result.missingSections());
        response.setWeakContentSignals(result.weakSignals());
        response.setStrengths(result.strengths());
        response.setExtractedSkills(result.extractedSkills());
        response.setSkillGapSkills(result.skillGapSkills());
        response.setCategoryBreakdown(result.categoryBreakdown());
        return response;
    }

    private ResumeAnalysisResult parseAnalysisJson(String json) {
        if (json == null || json.isBlank()) {
            return new ResumeAnalysisResult(0, 0, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), "");
        }

        String[] segments = json.split("\\|\\|\\|");
        if (segments.length < 9) {
            return new ResumeAnalysisResult(0, 0, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), "");
        }

        int profileCompleteness = Integer.parseInt(segments[0]);
        String missingSectionsRaw = segments[1];
        String weakSignalsRaw = segments[2];
        String strengthsRaw = segments[3];
        String extractedSkillsRaw = segments[4];
        String skillGapSkillsRaw = segments[5];
        String categoryBreakdownRaw = segments[6];
        String summary = segments[7];
        String overall = segments[8];

        List<String> missingSections = missingSectionsRaw.isBlank() ? List.of() : Arrays.stream(missingSectionsRaw.split(";"))
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<String> weakSignals = weakSignalsRaw.isBlank() ? List.of() : Arrays.stream(weakSignalsRaw.split(";"))
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<String> strengths = strengthsRaw.isBlank() ? List.of() : Arrays.stream(strengthsRaw.split(";"))
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<String> extractedSkillsParsed = extractedSkillsRaw.isBlank() ? List.of() : Arrays.stream(extractedSkillsRaw.split(";"))
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<String> skillGapSkillsParsed = skillGapSkillsRaw.isBlank() ? List.of() : Arrays.stream(skillGapSkillsRaw.split(";"))
                .filter(part -> !part.isBlank())
                .map(String::trim)
                .toList();
        List<ResumeAnalysisResponse.ResumeCategoryScore> categoryBreakdown = categoryBreakdownRaw.isBlank() ? List.of() : Arrays.stream(categoryBreakdownRaw.split("~"))
                .filter(part -> !part.isBlank())
                .map(part -> {
                    String[] fields = part.split("\\|");
                    if (fields.length < 4) return null;
                    try {
                        return new ResumeAnalysisResponse.ResumeCategoryScore(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), fields[3]);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(item -> item != null)
                .toList();

        return new ResumeAnalysisResult(
                overall.isBlank() ? 0 : Integer.parseInt(overall),
                profileCompleteness,
                missingSections,
                weakSignals,
                strengths,
                extractedSkillsParsed,
                skillGapSkillsParsed,
                categoryBreakdown,
                summary
        );
    }

    private record ResumeAnalysisResult(int overallScore, int profileCompleteness, List<String> missingSections,
                                       List<String> weakSignals, List<String> strengths,
                                       List<String> extractedSkills, List<String> skillGapSkills,
                                       List<ResumeAnalysisResponse.ResumeCategoryScore> categoryBreakdown,
                                       String summary) {
        public String toJson() {
            return profileCompleteness + "|||"
                    + String.join(";", missingSections) + "|||"
                    + String.join(";", weakSignals) + "|||"
                    + String.join(";", strengths) + "|||"
                    + String.join(";", extractedSkills) + "|||"
                    + String.join(";", skillGapSkills) + "|||"
                    + categoryBreakdown.stream().map(item -> item.getName() + "|" + item.getScore() + "|" + item.getWeight() + "|" + item.getDescription())
                        .collect(Collectors.joining("~")) + "|||"
                    + summary + "|||"
                    + overallScore;
        }
    }
}
