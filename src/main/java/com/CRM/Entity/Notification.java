package com.CRM.Entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String message;

    private Boolean isRead;

    @ManyToOne
    private User user;

    @ManyToOne
    private Organization organization;

    private LocalDateTime createdAt;}
