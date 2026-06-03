package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deals")
@Data
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private Double value;

    @Enumerated(EnumType.STRING)
    private DealStage stage;

    @ManyToOne
    private Lead lead;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private Organization organization;

    private LocalDate expectedCloseDate;

    private LocalDateTime createdAt;
}
