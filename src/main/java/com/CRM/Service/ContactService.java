package com.CRM.Service;

import com.CRM.DTO.ContactResponse;
import com.CRM.DTO.CreateContactRequest;
import com.CRM.DTO.UpdateContactRequest;
import com.CRM.Entity.Account;
import com.CRM.Entity.Contact;
import com.CRM.Entity.User;
import com.CRM.Repo.AccountRepo;
import com.CRM.Repo.ContactRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepo contactRepo;
    private final AccountRepo accountRepo;
    private final UserRepo userRepo;

    public ContactResponse createContact(CreateContactRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepo.findById(request.getAccountId()).orElse(null);
            if (account != null && !account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Unauthorized account assignment");
            }
        }

        User assignedUser = currentUser;
        if (request.getAssignedToUserId() != null) {
            assignedUser = userRepo.findById(request.getAssignedToUserId()).orElse(currentUser);
        }

        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobTitle(request.getJobTitle())
                .account(account)
                .organization(currentUser.getOrganization())
                .assignedTo(assignedUser)
                .createdAt(LocalDateTime.now())
                .build();

        Contact savedContact = contactRepo.save(contact);
        return mapToResponse(savedContact);
    }

    public List<ContactResponse> getAllContacts(String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        return contactRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ContactResponse getContactById(UUID contactId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        Contact contact = contactRepo.findById(contactId).orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return mapToResponse(contact);
    }

    public ContactResponse updateContact(UUID contactId, UpdateContactRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        Contact contact = contactRepo.findById(contactId).orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        if (request.getFirstName() != null) contact.setFirstName(request.getFirstName());
        if (request.getLastName() != null) contact.setLastName(request.getLastName());
        if (request.getEmail() != null) contact.setEmail(request.getEmail());
        if (request.getPhone() != null) contact.setPhone(request.getPhone());
        if (request.getJobTitle() != null) contact.setJobTitle(request.getJobTitle());

        if (request.getAccountId() != null) {
            Account account = accountRepo.findById(request.getAccountId()).orElseThrow(() -> new RuntimeException("Account not found"));
            if (!account.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new RuntimeException("Unauthorized account assignment");
            }
            contact.setAccount(account);
        }

        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepo.findById(request.getAssignedToUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            contact.setAssignedTo(assignedUser);
        }

        Contact updatedContact = contactRepo.save(contact);
        return mapToResponse(updatedContact);
    }

    public void deleteContact(UUID contactId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        Contact contact = contactRepo.findById(contactId).orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        contactRepo.delete(contact);
    }

    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .jobTitle(contact.getJobTitle())
                .accountId(contact.getAccount() != null ? contact.getAccount().getId() : null)
                .accountName(contact.getAccount() != null ? contact.getAccount().getCompanyName() : null)
                .assignedToUserId(contact.getAssignedTo() != null ? contact.getAssignedTo().getId() : null)
                .assignedToUserName(contact.getAssignedTo() != null ? (contact.getAssignedTo().getFirstName() + " " + contact.getAssignedTo().getLastName()) : null)
                .createdAt(contact.getCreatedAt())
                .build();
    }
}
