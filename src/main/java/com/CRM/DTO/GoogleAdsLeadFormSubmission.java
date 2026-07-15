package com.CRM.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class GoogleAdsLeadFormSubmission {
    private String submissionId;
    private String formId;
    private Map<String, String> fieldData;
    private String campaignId;
    private String adGroupId;
    private String adId;
    private String leadCreatedAt;
}
