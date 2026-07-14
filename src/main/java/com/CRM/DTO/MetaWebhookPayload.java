package com.CRM.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaWebhookPayload {

    private String object;
    private List<Entry> entry;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private Long time;
        private List<Change> changes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private String field;
        private Value value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("leadgen_id")
        private String leadgenId;

        @JsonProperty("page_id")
        private String pageId;

        @JsonProperty("form_id")
        private String formId;

        @JsonProperty("created_time")
        private Long createdTime;
    }
}
