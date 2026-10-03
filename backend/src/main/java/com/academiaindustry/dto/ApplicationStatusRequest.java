package com.academiaindustry.dto;

import com.academiaindustry.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusRequest {

    @NotNull
    private ApplicationStatus status;

    public ApplicationStatusRequest() {
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}