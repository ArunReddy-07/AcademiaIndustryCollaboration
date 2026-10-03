package com.academiaindustry.dto;

import com.academiaindustry.entity.Role;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String companyName;
    private String website;
    private String industrySector;
    private String headquarters;
    private String companyDescription;
    private Long facultyInstitutionId;
    private String facultyInstitutionName;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getIndustrySector() { return industrySector; }
    public void setIndustrySector(String industrySector) { this.industrySector = industrySector; }
    public String getHeadquarters() { return headquarters; }
    public void setHeadquarters(String headquarters) { this.headquarters = headquarters; }
    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }
    public Long getFacultyInstitutionId() { return facultyInstitutionId; }
    public void setFacultyInstitutionId(Long facultyInstitutionId) { this.facultyInstitutionId = facultyInstitutionId; }
    public String getFacultyInstitutionName() { return facultyInstitutionName; }
    public void setFacultyInstitutionName(String facultyInstitutionName) { this.facultyInstitutionName = facultyInstitutionName; }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}