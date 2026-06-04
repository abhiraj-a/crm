package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String companyName;
    private String industry;
    private String website;
    private String employeeCount;
    private String annualRevenue;

    @Column(columnDefinition = "TEXT")
    private String description;

    // CUSTOMER HIERARCHY: Links to another Account in the same table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private Account parentAccount;

    // TENANT ISOLATION & OWNERSHIP
    @ManyToOne(fetch = FetchType.LAZY)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedTo;

    private LocalDateTime createdAt;
}