package com.CRM.Repo;

import com.CRM.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepo extends JpaRepository<Note, UUID> {
    List<Note> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
    List<Note> findByDealIdOrderByCreatedAtDesc(UUID dealId);
}
