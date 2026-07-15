package com.CRM.Repo;

import com.CRM.Entity.GoogleAdsIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoogleAdsIntegrationRepo extends JpaRepository<GoogleAdsIntegration, Long> {
    Optional<GoogleAdsIntegration> findByUserIdAndGoogleCustomerId(Long userId, String googleCustomerId);
    List<GoogleAdsIntegration> findByUserId(Long userId);
    List<GoogleAdsIntegration> findByIsActiveTrue();
}
