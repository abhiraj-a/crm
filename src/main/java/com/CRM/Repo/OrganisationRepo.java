package com.CRM.Repo;

import com.CRM.Entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganisationRepo extends JpaRepository<Organization, UUID> {


}
