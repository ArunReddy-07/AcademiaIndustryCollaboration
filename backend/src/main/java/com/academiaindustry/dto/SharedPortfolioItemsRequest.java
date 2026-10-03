package com.academiaindustry.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SharedPortfolioItemsRequest {

    @Size(max = 50)
    private List<@NotNull @Positive Long> sharedPortfolioItemIds = List.of();

    public List<Long> getSharedPortfolioItemIds() {
        return sharedPortfolioItemIds;
    }

    public void setSharedPortfolioItemIds(List<Long> sharedPortfolioItemIds) {
        this.sharedPortfolioItemIds = sharedPortfolioItemIds == null ? List.of() : sharedPortfolioItemIds;
    }
}
