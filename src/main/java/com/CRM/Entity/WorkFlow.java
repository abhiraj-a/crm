package com.CRM.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflows")
@Data
public class WorkFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String description;

    private Boolean active;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;
}
