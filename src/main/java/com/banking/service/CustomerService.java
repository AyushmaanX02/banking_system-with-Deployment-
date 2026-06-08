package com.banking.service;

import com.banking.dto.response.CustomerResponse;
import com.banking.entity.User;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;

    public CustomerResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return CustomerResponse.from(user);
    }

    public CustomerResponse updatePhone(String email, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setPhone(phone);
        User savedUser = userRepository.save(user);
        return CustomerResponse.from(savedUser);
    }
}
