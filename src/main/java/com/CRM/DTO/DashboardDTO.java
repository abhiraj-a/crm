package com.CRM.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public class DashboardDTO {

    // KPI Summary Metrics
    private Double totalPipelineValue;
    private Long newLeadsCount;
    private Long pendingTasks;

    // Quick Access Lists
    private List<SimpleLead> topLeads;
    private List<SimpleTask> upcomingTasks;

    @Data
    @Builder
    public static class SimpleLead {
        private String name;
        private String company;
        private Integer score;
    }

    @Data
    @Builder
    public static class SimpleTask {
        private String title;
        private String deadline;
    }
}
