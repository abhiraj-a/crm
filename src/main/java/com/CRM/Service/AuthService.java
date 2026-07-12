package com.CRM.Service;

import com.CRM.DTO.SignupRequest;
import com.CRM.Entity.InviteToken;
import com.CRM.Entity.Organization;
import com.CRM.Entity.Role;
import com.CRM.Entity.User;
import com.CRM.Repo.InviteTokenRepo;
import com.CRM.Repo.OrganisationRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final OrganisationRepo organizationRepo;
    private final InviteTokenRepo inviteTokenRepo;

    @Transactional
    public void signup(SignupRequest signupRequest) {
        if (userRepo.existsByAuthifyerId(signupRequest.getAuthifyerId())) {
            throw new RuntimeException("User already exists");
        }

        // ── Path 1: Invite code provided → join existing workspace ──
        if (signupRequest.getInviteCode() != null && !signupRequest.getInviteCode().isBlank()) {
            signupWithInvite(signupRequest);
            return;
        }

        // ── Path 2: No invite code → create a new organization ──
        Organization organization = Organization.builder()
                .companyName(signupRequest.getCompanyName())
                .companyAddress(signupRequest.getCompanyAddress())
                .createdAt(LocalDateTime.now())
                .companySize(signupRequest.getCompanySize())
                .build();
        organizationRepo.save(organization);

        User user = User.builder()
                .createdAt(LocalDateTime.now())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLasName())
                .authifyerId(signupRequest.getAuthifyerId())
                .organization(organization)
                .phoneNumber(signupRequest.getPhoneNumber())
                .jobTitle(signupRequest.getJobTitle())
                .role(Role.ADMIN)
                .email(signupRequest.getEmail())
                .build();
        userRepo.save(user);
    }

    private void signupWithInvite(SignupRequest signupRequest) {
        InviteToken invite = inviteTokenRepo.findByToken(signupRequest.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code."));

        if (invite.isAccepted()) {
            throw new RuntimeException("This invitation has already been used.");
        }

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This invitation has expired.");
        }

        User user = User.builder()
                .createdAt(LocalDateTime.now())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLasName())
                .authifyerId(signupRequest.getAuthifyerId())
                .organization(invite.getOrganization())
                .phoneNumber(signupRequest.getPhoneNumber())
                .jobTitle(signupRequest.getJobTitle())
                .role(invite.getRole())
                .email(signupRequest.getEmail())
                .build();
        userRepo.save(user);

        invite.setAccepted(true);
        inviteTokenRepo.save(invite);
    }
}

