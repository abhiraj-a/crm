package com.CRM.Repo;

import com.CRM.Entity.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteTokenRepo extends JpaRepository<InviteToken, UUID> {
    Optional<InviteToken> findByToken(String token);

    List<InviteToken> findByOrganizationIdAndAcceptedFalse(UUID orgId);

    boolean existsByEmailAndOrganizationIdAndAcceptedFalse(String email, UUID orgId);
}
