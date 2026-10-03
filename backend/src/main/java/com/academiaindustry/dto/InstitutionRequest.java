package com.academiaindustry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstitutionRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String website;

    @Size(max = 200)
    private String location;

    @Size(max = 2000)
    private String description;

    public InstitutionRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}