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

    public void signup(SignupRequest signupRequest){
        if(userRepo.existsByAuthifyerId(signupRequest.getAuthifyerId())){
            throw new RuntimeException("User already exists");
        }
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
}
