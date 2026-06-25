package com.CRM.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflows")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
