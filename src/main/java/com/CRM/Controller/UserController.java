package com.CRM.Controller;

import com.CRM.DTO.DashboardDTO;
import com.CRM.Service.UserService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@AuthenticationPrincipal Principal principal){
        DashboardDTO dashboardDTO =  userService.getDashboard(principal);
        return ResponseEntity.ok(dashboardDTO);
    }
}
