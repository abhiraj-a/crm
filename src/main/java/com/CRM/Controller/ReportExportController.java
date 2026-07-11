package com.CRM.Controller;

import com.CRM.Service.ReportExportService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ReportExportController {

    private final ReportExportService reportExportService;

    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/leads")
    public ResponseEntity<?> exportLeads(@AuthenticationPrincipal Principal principal) {
        try {
            String csv = reportExportService.exportLeadsCsv(principal.getAuthifyerId());
            return buildCsvResponse(csv, "leads");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/deals")
    public ResponseEntity<?> exportDeals(@AuthenticationPrincipal Principal principal) {
        try {
            String csv = reportExportService.exportDealsCsv(principal.getAuthifyerId());
            return buildCsvResponse(csv, "deals");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> exportAccounts(@AuthenticationPrincipal Principal principal) {
        try {
            String csv = reportExportService.exportAccountsCsv(principal.getAuthifyerId());
            return buildCsvResponse(csv, "accounts");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> exportTasks(@AuthenticationPrincipal Principal principal) {
        try {
            String csv = reportExportService.exportTasksCsv(principal.getAuthifyerId());
            return buildCsvResponse(csv, "tasks");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> exportTickets(@AuthenticationPrincipal Principal principal) {
        try {
            String csv = reportExportService.exportTicketsCsv(principal.getAuthifyerId());
            return buildCsvResponse(csv, "tickets");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Builds a ResponseEntity that triggers a browser CSV file download.
     * Filename format: {type}_report_2026-07-11.csv
     */
    private ResponseEntity<byte[]> buildCsvResponse(String csvContent, String reportType) {
        String filename = reportType + "_report_" + LocalDate.now().format(FILE_DATE_FMT) + ".csv";
        byte[] bytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
