package com.banking.service;

import com.banking.dto.request.BeneficiaryRequest;
import com.banking.dto.response.BeneficiaryResponse;
import com.banking.entity.Beneficiary;
import com.banking.entity.User;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.BeneficiaryRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    public BeneficiaryResponse addBeneficiary(String email, BeneficiaryRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Beneficiary beneficiary = Beneficiary.builder()
                .nickname(request.getNickname())
                .accountNumber(request.getAccountNumber())
                .bankName(request.getBankName())
                .user(user)
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);
        return BeneficiaryResponse.from(savedBeneficiary);
    }

    public List<BeneficiaryResponse> getMyBeneficiaries(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<Beneficiary> beneficiaries = beneficiaryRepository.findByUser(user);
        return beneficiaries.stream()
                .map(BeneficiaryResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteBeneficiary(String email, Long id) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found with ID: " + id));

        if (!beneficiary.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to delete this beneficiary");
        }

        beneficiaryRepository.delete(beneficiary);
    }
}
