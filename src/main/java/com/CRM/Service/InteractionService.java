package com.CRM.Service;

import com.CRM.DTO.CreateInteractionRequest;
import com.CRM.Entity.Interaction;
import com.CRM.Entity.Lead;
import com.CRM.Entity.User;
import com.CRM.Repo.InteractionRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepo interactionRepo;
    private final LeadRepo leadRepo;
    private final UserRepo userRepo;

    /**
     * Logs a customer interaction (call, meeting, etc.) linked to a lead.
     */
    @Transactional
    public Interaction createInteraction(CreateInteractionRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lead lead = leadRepo.findById(request.getLeadId())
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Lead belongs to a different organization.");
        }

        Interaction interaction = Interaction.builder()
                .type(request.getType())
                .subject(request.getSubject())
                .details(request.getDetails())
                .timestamp(LocalDateTime.now())
                .performedBy(currentUser)
                .lead(lead)
                .organization(currentUser.getOrganization())
                .build();

        return interactionRepo.save(interaction);
    }
}
