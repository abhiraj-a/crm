package com.CRM.Repository;

import com.CRM.Entity.MetaIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaIntegrationRepository extends JpaRepository<MetaIntegration, Long> {
    Optional<MetaIntegration> findByPageId(String pageId);
    List<MetaIntegration> findByUserId(Long userId);
}
