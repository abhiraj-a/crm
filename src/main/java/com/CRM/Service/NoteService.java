package com.CRM.Service;

import com.CRM.DTO.CreateNoteRequest;
import com.CRM.Entity.*;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.NoteRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepo noteRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;
    private final UserRepo userRepo;

    /**
     * Creates a note linked to a lead and/or deal.
     * At least one of leadId or dealId must be provided.
     */
    @Transactional
    public Note createNote(CreateNoteRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getLeadId() == null && request.getDealId() == null) {
            throw new RuntimeException("A note must be linked to at least a lead or a deal.");
        }

        Lead lead = null;
        if (request.getLeadId() != null) {
            lead = leadRepo.findById(request.getLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead not found"));
            if (!lead.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Unauthorized access: Lead belongs to a different organization.");
            }
        }

        Deal deal = null;
        if (request.getDealId() != null) {
            deal = dealRepo.findById(request.getDealId())
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            if (!deal.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Unauthorized access: Deal belongs to a different organization.");
            }
        }

        Note note = Note.builder()
                .content(request.getContent())
                .createdBy(currentUser)
                .lead(lead)
                .deal(deal)
                .organization(currentUser.getOrganization())
                .createdAt(LocalDateTime.now())
                .build();

        return noteRepo.save(note);
    }
}
