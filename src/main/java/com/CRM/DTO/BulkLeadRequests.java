package com.CRM.DTO;

import lombok.Data;
import java.util.List;
import java.util.UUID;

public class BulkLeadRequests {

    @Data
    public static class BulkEmailRequest {
        private List<UUID> leadIds;
        private String subject;
        private String body;
    }

    @Data
    public static class BulkAssignRequest {
        private List<UUID> leadIds;
        private UUID assigneeId;
    }

    @Data
    public static class MergeLeadsRequest {
        private UUID primaryLeadId;
        private List<UUID> secondaryLeadIds;
    }

    @Data
    public static class BulkDeleteRequest {
        private List<UUID> leadIds;
    }
}
