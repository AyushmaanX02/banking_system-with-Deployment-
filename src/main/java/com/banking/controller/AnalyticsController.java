package com.banking.controller;

import com.banking.dto.response.AnalyticsSummaryResponse;
import com.banking.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for transaction analytics and spending insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(
        summary = "Get analytics summary",
        description = "Returns last 6 months of monthly totals, all-time sums, and balance history for the authenticated user"
    )
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(Authentication authentication) {
        AnalyticsSummaryResponse response = analyticsService.getSummaryForUser(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
