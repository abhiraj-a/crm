package com.CRM.Service;

import com.CRM.DTO.*;
import com.CRM.Entity.*;
import com.CRM.Repo.InviteTokenRepo;
import com.CRM.Repo.OrganisationRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final UserRepo userRepo;
    private final InviteTokenRepo inviteTokenRepo;
    private final OrganisationRepo organizationRepo;
    private final EmailService emailService;

    private User getCurrentUser(String authifyerId) {
        return userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void requireRole(User user, Role... allowed) {
        for (Role r : allowed) {
            if (user.getRole() == r) return;
        }
        throw new RuntimeException("You do not have permission to perform this action.");
    }

    // ── List team members ──
    public List<TeamMemberResponse> listTeamMembers(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return userRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream().map(this::mapToMemberResponse).collect(Collectors.toList());
    }

    // ── Invite a user ──
    @Transactional
    public InviteResponse inviteUser(InviteRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        requireRole(currentUser, Role.ADMIN, Role.MANAGER);

        // Managers can only invite SALES_REPs
        if (currentUser.getRole() == Role.MANAGER && request.getRole() != Role.SALES_REP) {
            throw new RuntimeException("Managers can only invite Sales Reps.");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Check if user already exists in this org
        if (userRepo.existsByEmailAndOrganizationId(email, currentUser.getOrganization().getId())) {
            throw new RuntimeException("A user with this email already belongs to your organization.");
        }

        // Check if there's already a pending invite for this email
        if (inviteTokenRepo.existsByEmailAndOrganizationIdAndAcceptedFalse(email, currentUser.getOrganization().getId())) {
            throw new RuntimeException("An invitation has already been sent to this email.");
        }

        InviteToken inviteToken = InviteToken.builder()
                .email(email)
                .role(request.getRole() != null ? request.getRole() : Role.SALES_REP)
                .organization(currentUser.getOrganization())
                .invitedBy(currentUser)
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .accepted(false)
                .build();

        inviteTokenRepo.save(inviteToken);

        // Send invite email
        String orgName = currentUser.getOrganization().getCompanyName();
        String inviterName = currentUser.getFirstName() + " " + currentUser.getLastName();
        emailService.sendEmail(
                email,
                "You're invited to join " + orgName + " on CRM",
                "Hi,\n\n" + inviterName + " has invited you to join \"" + orgName + "\" as a "
                        + inviteToken.getRole().name().replace("_", " ") + ".\n\n"
                        + "Your invite code is: " + inviteToken.getToken() + "\n\n"
                        + "Sign up and use this code to join the team.\n\n"
                        + "This invitation expires in 7 days."
        );

        return mapToInviteResponse(inviteToken);
    }

    // ── Accept an invite ──
    @Transactional
    public TeamMemberResponse acceptInvite(String token, String authifyerId) {
        InviteToken invite = inviteTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invite code."));

        if (invite.isAccepted()) {
            throw new RuntimeException("This invitation has already been used.");
        }

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This invitation has expired.");
        }

        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElse(null);

        if (currentUser != null && currentUser.getOrganization() != null) {
            // User already belongs to an organization — update their org
            if (currentUser.getOrganization().getId().equals(invite.getOrganization().getId())) {
                throw new RuntimeException("You are already a member of this organization.");
            }
        }

        if (currentUser == null) {
            // User signed up through Authifyer but hasn't registered in CRM yet
            currentUser = User.builder()
                    .authifyerId(authifyerId)
                    .email(invite.getEmail())
                    .firstName("")
                    .lastName("")
                    .role(invite.getRole())
                    .organization(invite.getOrganization())
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            currentUser.setOrganization(invite.getOrganization());
            currentUser.setRole(invite.getRole());
        }

        userRepo.save(currentUser);

        invite.setAccepted(true);
        inviteTokenRepo.save(invite);

        return mapToMemberResponse(currentUser);
    }

    // ── Update member role ──
    @Transactional
    public TeamMemberResponse updateMemberRole(UUID userId, Role newRole, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        requireRole(currentUser, Role.ADMIN);

        User target = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!target.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("User does not belong to your organization.");
        }

        if (target.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot change your own role.");
        }

        target.setRole(newRole);
        return mapToMemberResponse(userRepo.save(target));
    }

    // ── Remove a member ──
    @Transactional
    public void removeMember(UUID userId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        requireRole(currentUser, Role.ADMIN);

        User target = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!target.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("User does not belong to your organization.");
        }

        if (target.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot remove yourself from the organization.");
        }

        userRepo.delete(target);
    }

    // ── List pending invites ──
    public List<InviteResponse> listPendingInvites(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return inviteTokenRepo.findByOrganizationIdAndAcceptedFalse(currentUser.getOrganization().getId())
                .stream()
                .filter(i -> i.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::mapToInviteResponse)
                .collect(Collectors.toList());
    }

    // ── Revoke a pending invite ──
    @Transactional
    public void revokeInvite(UUID inviteId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        requireRole(currentUser, Role.ADMIN, Role.MANAGER);

        InviteToken invite = inviteTokenRepo.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found."));

        if (!invite.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("This invite does not belong to your organization.");
        }

        inviteTokenRepo.delete(invite);
    }

    // ── Get current user profile ──
    public MyProfileResponse getMyProfile(String authifyerId) {
        User user = getCurrentUser(authifyerId);
        return MyProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .phoneNumber(user.getPhoneNumber())
                .orgName(user.getOrganization().getCompanyName())
                .orgAddress(user.getOrganization().getCompanyAddress())
                .orgSize(user.getOrganization().getCompanySize())
                .orgId(user.getOrganization().getId())
                .build();
    }

    // ── Update organization details (Admin only) ──
    @Transactional
    public MyProfileResponse updateOrganization(UpdateOrganizationRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        requireRole(currentUser, Role.ADMIN);

        Organization org = currentUser.getOrganization();

        if (request.getCompanyName() != null) org.setCompanyName(request.getCompanyName());
        if (request.getCompanyAddress() != null) org.setCompanyAddress(request.getCompanyAddress());
        if (request.getCompanySize() != null) org.setCompanySize(request.getCompanySize());

        organizationRepo.save(org);

        return getMyProfile(authifyerId);
    }

    // ── Update own profile ──
    @Transactional
    public MyProfileResponse updateMyProfile(UpdateProfileRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        if (request.getFirstName() != null) currentUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) currentUser.setLastName(request.getLastName());
        if (request.getJobTitle() != null) currentUser.setJobTitle(request.getJobTitle());
        if (request.getPhoneNumber() != null) currentUser.setPhoneNumber(request.getPhoneNumber());

        userRepo.save(currentUser);

        return getMyProfile(authifyerId);
    }

    // ── Mappers ──
    private TeamMemberResponse mapToMemberResponse(User user) {
        return TeamMemberResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .jobTitle(user.getJobTitle())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private InviteResponse mapToInviteResponse(InviteToken invite) {
        User inviter = invite.getInvitedBy();
        return InviteResponse.builder()
                .id(invite.getId())
                .email(invite.getEmail())
                .role(invite.getRole())
                .invitedByName(inviter != null ? inviter.getFirstName() + " " + inviter.getLastName() : "Unknown")
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}
