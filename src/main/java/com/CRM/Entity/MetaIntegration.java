package com.CRM.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meta_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "page_id", nullable = false, unique = true)
    private String pageId;

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "page_access_token", nullable = false, columnDefinition = "TEXT")
    private String pageAccessToken;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
