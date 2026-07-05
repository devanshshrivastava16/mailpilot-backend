package com.mailpilot.controller;

import com.mailpilot.dto.SenderAccountRequest;
import com.mailpilot.dto.SenderAccountResponse;
import com.mailpilot.security.CustomUserDetailsService;
import com.mailpilot.service.SenderAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sender-accounts")
@RequiredArgsConstructor
public class SenderAccountController {

    private final SenderAccountService senderAccountService;
    private final CustomUserDetailsService userDetailsService;
    // We need UserRepository to get userId from email. Add a UserService or use repo directly.
    // Actually let's add a method in the service. For now, use a simple approach.

    // We'll inject UserRepository for userId lookup
    private final com.mailpilot.repository.UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
    }

    @GetMapping
    public ResponseEntity<List<SenderAccountResponse>> getAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(senderAccountService.getAccounts(getUserId(userDetails)));
    }

    @PostMapping
    public ResponseEntity<SenderAccountResponse> addAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SenderAccountRequest request) {
        return ResponseEntity.ok(senderAccountService.addAccount(getUserId(userDetails), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        senderAccountService.deleteAccount(getUserId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<SenderAccountResponse> setActiveAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(senderAccountService.setActiveAccount(getUserId(userDetails), id));
    }
}