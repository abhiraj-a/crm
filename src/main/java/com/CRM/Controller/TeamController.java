package com.CRM.Controller;

import com.CRM.DTO.InviteRequest;
import com.CRM.Entity.Role;
import com.CRM.Service.TeamService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<?> listTeamMembers(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(teamService.listTeamMembers(principal.getAuthifyerId()));
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteRequest request,
                                         @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(teamService.inviteUser(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvite(@RequestParam String token,
                                           @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(teamService.acceptInvite(token, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateMemberRole(@PathVariable UUID userId,
                                                @RequestParam Role role,
                                                @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(teamService.updateMemberRole(userId, role, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable UUID userId,
                                            @AuthenticationPrincipal Principal principal) {
        try {
            teamService.removeMember(userId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Member removed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invites")
    public ResponseEntity<?> listPendingInvites(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(teamService.listPendingInvites(principal.getAuthifyerId()));
    }

    @DeleteMapping("/invites/{inviteId}")
    public ResponseEntity<?> revokeInvite(@PathVariable UUID inviteId,
                                            @AuthenticationPrincipal Principal principal) {
        try {
            teamService.revokeInvite(inviteId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Invite revoked successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(teamService.getMyProfile(principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
