package com.CRM.Service;

import com.CRM.Entity.*;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;
    private final AccountRepo accountRepo;
    private final TaskRepo taskRepo;
    private final TicketRepo ticketRepo;
    private final UserRepo userRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─── Leads ───────────────────────────────────────────────────────────

    public String exportLeadsCsv(String authifyerId) {
        UUID orgId = getOrgId(authifyerId);
        List<Lead> leads = leadRepo.findByOrganizationId(orgId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Email,Phone,Company,Status,Source,Score,Assigned To,Created At\n");

        for (Lead l : leads) {
            csv.append(escape(str(l.getId()))).append(',');
            csv.append(escape(l.getName())).append(',');
            csv.append(escape(l.getEmail())).append(',');
            csv.append(escape(l.getPhone())).append(',');
            csv.append(escape(l.getCompany())).append(',');
            csv.append(escape(str(l.getStatus()))).append(',');
            csv.append(escape(l.getSource())).append(',');
            csv.append(escape(str(l.getScore()))).append(',');
            csv.append(escape(userName(l.getAssignedTo()))).append(',');
            csv.append(escape(fmtDateTime(l.getCreatedAt()))).append('\n');
        }

        return csv.toString();
    }

    // ─── Deals ───────────────────────────────────────────────────────────

    public String exportDealsCsv(String authifyerId) {
        UUID orgId = getOrgId(authifyerId);
        List<Deal> deals = dealRepo.findByOrganizationId(orgId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Value,Stage,Lead Name,Assigned To,Account,Expected Close Date,Created At\n");

        for (Deal d : deals) {
            csv.append(escape(str(d.getId()))).append(',');
            csv.append(escape(d.getTitle())).append(',');
            csv.append(escape(str(d.getValue()))).append(',');
            csv.append(escape(str(d.getStage()))).append(',');
            csv.append(escape(d.getLead() != null ? d.getLead().getName() : "")).append(',');
            csv.append(escape(userName(d.getAssignedTo()))).append(',');
            csv.append(escape(d.getAccount() != null ? d.getAccount().getCompanyName() : "")).append(',');
            csv.append(escape(fmtDate(d.getExpectedCloseDate()))).append(',');
            csv.append(escape(fmtDateTime(d.getCreatedAt()))).append('\n');
        }

        return csv.toString();
    }

    // ─── Accounts ────────────────────────────────────────────────────────

    public String exportAccountsCsv(String authifyerId) {
        UUID orgId = getOrgId(authifyerId);
        List<Account> accounts = accountRepo.findByOrganizationId(orgId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Company Name,Industry,Website,Employee Count,Annual Revenue,Assigned To,Parent Account,Created At\n");

        for (Account a : accounts) {
            csv.append(escape(str(a.getId()))).append(',');
            csv.append(escape(a.getCompanyName())).append(',');
            csv.append(escape(a.getIndustry())).append(',');
            csv.append(escape(a.getWebsite())).append(',');
            csv.append(escape(a.getEmployeeCount())).append(',');
            csv.append(escape(a.getAnnualRevenue())).append(',');
            csv.append(escape(userName(a.getAssignedTo()))).append(',');
            csv.append(escape(a.getParentAccount() != null ? a.getParentAccount().getCompanyName() : "")).append(',');
            csv.append(escape(fmtDateTime(a.getCreatedAt()))).append('\n');
        }

        return csv.toString();
    }

    // ─── Tasks ───────────────────────────────────────────────────────────

    public String exportTasksCsv(String authifyerId) {
        UUID orgId = getOrgId(authifyerId);
        List<Task> tasks = taskRepo.findByOrganizationId(orgId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Status,Deadline,Assigned To,Related Deal,Related Lead,Created At\n");

        for (Task t : tasks) {
            csv.append(escape(str(t.getId()))).append(',');
            csv.append(escape(t.getTitle())).append(',');
            csv.append(escape(t.getDescription())).append(',');
            csv.append(escape(str(t.getStatus()))).append(',');
            csv.append(escape(fmtDate(t.getDeadline()))).append(',');
            csv.append(escape(userName(t.getAssignedTo()))).append(',');
            csv.append(escape(t.getRelatedDeal() != null ? t.getRelatedDeal().getTitle() : "")).append(',');
            csv.append(escape(t.getRelatedLead() != null ? t.getRelatedLead().getName() : "")).append(',');
            csv.append(escape(fmtDateTime(t.getCreatedAt()))).append('\n');
        }

        return csv.toString();
    }

    // ─── Tickets ─────────────────────────────────────────────────────────

    public String exportTicketsCsv(String authifyerId) {
        UUID orgId = getOrgId(authifyerId);
        // Tickets don't have a findByOrganizationId — we need to add one, or filter in-memory.
        // For now, fetch all and filter by org. We'll add the repo method as well.
        List<Ticket> tickets = ticketRepo.findByOrganizationId(orgId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Subject,Description,Status,Priority,Assigned To,Lead Name,Created At,Closed At\n");

        for (Ticket t : tickets) {
            csv.append(escape(str(t.getId()))).append(',');
            csv.append(escape(t.getSubject())).append(',');
            csv.append(escape(t.getDescription())).append(',');
            csv.append(escape(str(t.getStatus()))).append(',');
            csv.append(escape(str(t.getPriority()))).append(',');
            csv.append(escape(userName(t.getAssignedTo()))).append(',');
            csv.append(escape(t.getLead() != null ? t.getLead().getName() : "")).append(',');
            csv.append(escape(fmtDateTime(t.getCreatedAt()))).append(',');
            csv.append(escape(fmtDateTime(t.getClosedAt()))).append('\n');
        }

        return csv.toString();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private UUID getOrgId(String authifyerId) {
        User user = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getOrganization().getId();
    }

    private String userName(User user) {
        if (user == null) return "Unassigned";
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private String fmtDateTime(java.time.LocalDateTime dt) {
        return dt != null ? dt.format(DATETIME_FMT) : "";
    }

    private String fmtDate(java.time.LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "";
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * RFC 4180 CSV escaping: if a field contains a comma, double-quote,
     * or newline, wrap it in double-quotes and escape internal quotes.
     */
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
