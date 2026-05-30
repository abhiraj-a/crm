package com.CRM.Service;

import com.CRM.DTO.SignupRequest;
import com.CRM.Entity.Organization;
import com.CRM.Entity.Role;
import com.CRM.Entity.User;
import com.CRM.Repo.OrganisationRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final OrganisationRepo organizationRepo;

    public void singup(SignupRequest signupRequest){
        Organization organization = Organization.builder()
                .companyName(signupRequest.getCompanyName())
                .companyAddress(signupRequest.getCompanyAddress())
                .createdAt(LocalDateTime.now())
                .companySize(signupRequest.getCompanySize())
                .orgId()
                .build();

        User user = User.builder()
                .createdAt(LocalDateTime.now())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLasName())
                .authifyerId(signupRequest.getAuthifyerId())
                .organization(organization)
                .phoneNumber(signupRequest.getPhoneNumber())
                .jobTitle(signupRequest.g)
                .role(Role.ADMIN)
                .email(signupRequest.getEmail())
                .build();

    }
}
