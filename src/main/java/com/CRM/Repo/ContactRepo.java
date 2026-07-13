package com.CRM.Repo;

import com.CRM.Entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactRepo extends JpaRepository<Contact, UUID> {
    List<Contact> findByOrganizationId(UUID organizationId);
    List<Contact> findByAccountId(UUID accountId);
}
