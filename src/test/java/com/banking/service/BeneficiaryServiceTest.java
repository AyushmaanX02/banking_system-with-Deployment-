package com.banking.service;

import com.banking.dto.request.BeneficiaryRequest;
import com.banking.dto.response.BeneficiaryResponse;
import com.banking.entity.Beneficiary;
import com.banking.entity.User;
import com.banking.enums.Role;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.BeneficiaryRepository;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryService Unit Tests")
class BeneficiaryServiceTest {

    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BeneficiaryService beneficiaryService;

    private User owner;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        beneficiary = Beneficiary.builder()
                .id(1L)
                .nickname("Receiver")
                .accountNumber("ACC999")
                .bankName("NexBank")
                .user(owner)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("addBeneficiary: valid request → beneficiary saved and returned")
    void addBeneficiary_success() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setNickname("Receiver");
        request.setAccountNumber("ACC999");
        request.setBankName("NexBank");

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);

        BeneficiaryResponse response = beneficiaryService.addBeneficiary("owner@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo("Receiver");
        assertThat(response.getAccountNumber()).isEqualTo("ACC999");
        verify(beneficiaryRepository).save(any(Beneficiary.class));
    }

    @Test
    @DisplayName("addBeneficiary: user not found → throws UsernameNotFoundException")
    void addBeneficiary_userNotFound_throws() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setNickname("X");
        request.setAccountNumber("ACC1");
        request.setBankName("Bank");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary("ghost@example.com", request))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMyBeneficiaries: user has 2 beneficiaries → list of 2 returned")
    void getMyBeneficiaries_returnsList() {
        Beneficiary beneficiary2 = Beneficiary.builder()
                .id(2L).nickname("Second").accountNumber("ACC888")
                .bankName("OtherBank").user(owner).createdAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.findByUser(owner)).thenReturn(List.of(beneficiary, beneficiary2));

        List<BeneficiaryResponse> result = beneficiaryService.getMyBeneficiaries("owner@example.com");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BeneficiaryResponse::getAccountNumber)
                .containsExactlyInAnyOrder("ACC999", "ACC888");
    }

    @Test
    @DisplayName("deleteBeneficiary: owner deletes own beneficiary → deleted")
    void deleteBeneficiary_ownerDeletes_success() {
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));

        beneficiaryService.deleteBeneficiary("owner@example.com", 1L);

        verify(beneficiaryRepository).delete(beneficiary);
    }

    @Test
    @DisplayName("deleteBeneficiary: non-owner attempts delete → throws UnauthorisedAccessException")
    void deleteBeneficiary_nonOwner_throws() {
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));

        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary("hacker@example.com", 1L))
                .isInstanceOf(UnauthorisedAccessException.class);
        verify(beneficiaryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteBeneficiary: ID not found → throws IllegalArgumentException")
    void deleteBeneficiary_notFound_throws() {
        when(beneficiaryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary("owner@example.com", 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}
