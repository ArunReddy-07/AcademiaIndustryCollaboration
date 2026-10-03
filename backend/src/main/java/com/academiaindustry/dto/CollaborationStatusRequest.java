package com.academiaindustry.dto;

import com.academiaindustry.entity.CollaborationStatus;
import jakarta.validation.constraints.NotNull;

public class CollaborationStatusRequest {
    @NotNull
    private CollaborationStatus status;

    public CollaborationStatusRequest() { }
    public CollaborationStatus getStatus() { return status; }
    public void setStatus(CollaborationStatus status) { this.status = status; }
}