package com.CRM.Repo;

import com.CRM.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {
    List<Account> findByOrganizationId(UUID organizationId);

    // Finds all subsidiary (child) companies of a specific parent account
    List<Account> findByParentAccountId(UUID parentAccountId);
}