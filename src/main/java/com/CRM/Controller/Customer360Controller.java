package com.CRM.Controller;

import com.CRM.DTO.TimeLineEvent;
import com.CRM.Service.Customer360Service;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer-360")
@RequiredArgsConstructor
public class Customer360Controller {

    private final Customer360Service customer360Service;

    @GetMapping("/{leadId}/timeline")
    public ResponseEntity<List<TimeLineEvent>> getTimeline(
            @PathVariable UUID leadId,
            @AuthenticationPrincipal Principal principal) {

        List<TimeLineEvent> timeline = customer360Service.getCustomerTimeline(leadId, principal.getAuthifyerId());
        return ResponseEntity.ok(timeline);
    }
}