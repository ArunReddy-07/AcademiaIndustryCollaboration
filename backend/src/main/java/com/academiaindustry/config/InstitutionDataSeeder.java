package com.academiaindustry.config;

import com.academiaindustry.entity.Institution;
import com.academiaindustry.repository.InstitutionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InstitutionDataSeeder {

    private static final List<String> DEFAULT_INSTITUTIONS = List.of(
        "Indian Institute of Technology Bombay",
        "Indian Institute of Technology Delhi",
        "Indian Institute of Technology Madras",
        "Indian Institute of Technology Kanpur",
        "Indian Institute of Technology Kharagpur",
        "Indian Institute of Technology Roorkee",
        "National Institute of Technology Tiruchirappalli",
        "Birla Institute of Technology and Science, Pilani",
        "Vellore Institute of Technology",
        "Anna University",
        "SRM Institute of Science and Technology",
        "Jadavpur University",
        "Demo University"
    );

    @Bean
    public CommandLineRunner seedInstitutions(InstitutionRepository institutionRepository) {
        return args -> {
            List<String> existingNames = institutionRepository.findAll().stream()
                .map(Institution::getName)
                .map(name -> name == null ? "" : name.trim())
                .filter(name -> !name.isEmpty())
                .map(String::toLowerCase)
                .toList();

            List<Institution> missing = DEFAULT_INSTITUTIONS.stream()
                .filter(name -> !existingNames.contains(name.toLowerCase()))
                .map(Institution::new)
                .toList();

            if (!missing.isEmpty()) {
                institutionRepository.saveAll(missing);
            }
        };
    }
}
