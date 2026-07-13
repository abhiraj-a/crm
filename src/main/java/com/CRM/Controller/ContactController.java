package com.CRM.Controller;

import com.CRM.DTO.ContactResponse;
import com.CRM.DTO.CreateContactRequest;
import com.CRM.DTO.UpdateContactRequest;
import com.CRM.Service.ContactService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody CreateContactRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            ContactResponse response = contactService.createContact(request, principal.getAuthifyerId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllContacts(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(contactService.getAllContacts(principal.getAuthifyerId()));
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<?> getContactById(@PathVariable UUID contactId, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(contactService.getContactById(contactId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<?> updateContact(@PathVariable UUID contactId, @RequestBody UpdateContactRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(contactService.updateContact(contactId, request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<?> deleteContact(@PathVariable UUID contactId, @AuthenticationPrincipal Principal principal) {
        try {
            contactService.deleteContact(contactId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Contact deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
