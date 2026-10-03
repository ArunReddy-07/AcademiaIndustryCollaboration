package com.academiaindustry.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @Size(max = 200)
    private String companyName;

    @Size(max = 1000)
    private String website;

    @Size(max = 150)
    private String industrySector;

    @Size(max = 200)
    private String headquarters;

    @Size(max = 2000)
    private String companyDescription;

    public ProfileUpdateRequest() {
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
}