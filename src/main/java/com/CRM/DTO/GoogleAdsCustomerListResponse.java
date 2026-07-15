package com.CRM.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GoogleAdsCustomerListResponse {
    @JsonProperty("resourceNames")
    private List<String> resourceNames;
}
