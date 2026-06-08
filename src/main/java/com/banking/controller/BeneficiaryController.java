package com.banking.controller;

import com.banking.dto.request.BeneficiaryRequest;
import com.banking.dto.response.BeneficiaryResponse;
import com.banking.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiaries", description = "Endpoints for managing customer beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @Operation(summary = "Add a beneficiary", description = "Register a new payee under the authenticated user's profile")
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(Authentication authentication, @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get beneficiaries", description = "Retrieve list of beneficiaries registered under the authenticated user")
    public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries(Authentication authentication) {
        List<BeneficiaryResponse> response = beneficiaryService.getMyBeneficiaries(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a beneficiary", description = "Remove a beneficiary by ID if owned by the authenticated user")
    public ResponseEntity<Void> deleteBeneficiary(Authentication authentication, @PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
