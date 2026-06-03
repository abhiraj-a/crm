package com.CRM.Event;

import com.CRM.Entity.Lead;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadCreatedEvent extends ApplicationEvent {
    private final Lead lead;
    private final String authifyerId; // Keep track of who triggered it
    public LeadCreatedEvent(Object source, Lead lead, String authifyerId) {
        super(source);
        this.lead = lead;
        this.authifyerId = authifyerId;
    }
}
