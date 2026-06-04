package com.CRM.Repo;

import com.CRM.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
