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

import java.util.List;

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
        List<DashboardDTO.SimpleLead> topLeads = leadRepo.findByOrganizationId(user.getOrganization().getId()).stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .limit(5)
                .map(l -> DashboardDTO.SimpleLead.builder()
                        .name(l.getName())
                        .company(l.getCompany())
                        .score(l.getScore())
                        .build())
                .toList();

        List<DashboardDTO.SimpleTask> upcomingTasks = taskRepo.findTop5ByAssignedToIdAndStatusNotOrderByDeadlineAsc(user.getId(), TaskStatus.COMPLETED)
                .stream()
                .map(t -> DashboardDTO.SimpleTask.builder()
                        .title(t.getTitle())
                        .deadline(t.getDeadline() != null ? t.getDeadline().toString() : null)
                        .build())
                .toList();

        DashboardDTO dashboardDTO = DashboardDTO.builder()
                 .totalPipelineValue(dealRepo.sumTotalPipelineValue(user.getOrganization().getId()))
                 .newLeadsCount(leadRepo.countByOrganizationIdAndStatus(user.getOrganization().getId(), LeadStatus.CONTACTED))
                 .pendingTasks(taskRepo.countByAssignedToIdAndStatusNot(user.getId(), TaskStatus.COMPLETED))
                 .topLeads(topLeads)
                 .upcomingTasks(upcomingTasks)
                .build();
        return dashboardDTO;
    }
}
