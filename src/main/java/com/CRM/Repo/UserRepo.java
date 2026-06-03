package com.CRM.Repo;

import com.CRM.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByAuthifyerId(String authifyerId);
}
