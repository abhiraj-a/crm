package com.CRM.Event;

import com.CRM.Entity.Deal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DealUpdatedEvent extends ApplicationEvent {
    private final Deal deal;
    private final String authifyerId;

    public DealUpdatedEvent(Object source, Deal deal, String authifyerId) {
        super(source);
        this.deal = deal;
        this.authifyerId = authifyerId;
    }
}
