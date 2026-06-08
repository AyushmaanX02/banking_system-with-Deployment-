package com.banking.controller;

import com.banking.dto.response.CustomerResponse;
import com.banking.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Endpoints for retrieving and editing user profiles")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Retrieve profile details of the authenticated user")
    public ResponseEntity<CustomerResponse> getProfile(Authentication authentication) {
        CustomerResponse response = customerService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/phone")
    @Operation(summary = "Update phone number", description = "Update the phone number of the authenticated user")
    public ResponseEntity<CustomerResponse> updatePhone(Authentication authentication, @RequestParam String phone) {
        CustomerResponse response = customerService.updatePhone(authentication.getName(), phone);
        return ResponseEntity.ok(response);
    }
}
