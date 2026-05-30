package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Builder
public class Organization {

    @Id
    @GeneratedValue
    private UUID id;
    private String orgId;
    private String companyName;
    private String companyAddress;
    private int companySize;
    private LocalDateTime createdAt;
}
