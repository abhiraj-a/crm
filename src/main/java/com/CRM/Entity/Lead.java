package com.CRM.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String email;

    private String phone;

    private String company;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    private String source;

    private Integer score;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;
}
