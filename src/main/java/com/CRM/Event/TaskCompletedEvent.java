package com.CRM.Event;

import com.CRM.Entity.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskCompletedEvent extends ApplicationEvent {
    private final Task task;
    private final String authifyerId;

    public TaskCompletedEvent(Object source, Task task, String authifyerId) {
        super(source);
        this.task = task;
        this.authifyerId = authifyerId;
    }
}
