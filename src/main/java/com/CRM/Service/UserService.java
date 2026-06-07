package com.CRM.Service;

import com.CRM.DTO.DashboardDTO;
import com.CRM.Entity.LeadStatus;
import com.CRM.Entity.TaskStatus;
import com.CRM.Entity.User;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.TaskRepo;
import com.CRM.Repo.UserRepo;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;
    private final TaskRepo taskRepo;

    public DashboardDTO getDashboard(Principal principal) {
        String authifyerId = principal.getAuthifyerId();
        User user = userRepo.findByAuthifyerId(authifyerId).orElseThrow(RuntimeException::new);
        DashboardDTO dashboardDTO = DashboardDTO.builder()
                 .totalPipelineValue(dealRepo.sumTotalPipelineValue(user.getOrganization().getId()))
                 .newLeadsCount(leadRepo.countByOrganizationIdAndStatus(user.getOrganization().getId(), LeadStatus.CONTACTED))
                 .pendingTasks(taskRepo.countByAssignedToIdAndStatusNot(user.getId(), TaskStatus.COMPLETED))
                .build();
        return dashboardDTO;
    }
}
