package com.CRM.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaAccountsResponse {

    private List<PageData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageData {
        @JsonProperty("access_token")
        private String accessToken;

        private String category;
        private String name;
        private String id;
    }
}
