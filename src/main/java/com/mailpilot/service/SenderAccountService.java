package com.mailpilot.service;

import com.mailpilot.dto.SenderAccountRequest;
import com.mailpilot.dto.SenderAccountResponse;
import com.mailpilot.entity.SenderAccount;
import com.mailpilot.entity.User;
import com.mailpilot.repository.SenderAccountRepository;
import com.mailpilot.repository.UserRepository;
import com.mailpilot.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SenderAccountService {

    private final SenderAccountRepository senderAccountRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<SenderAccountResponse> getAccounts(Long userId) {
        return senderAccountRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public SenderAccountResponse addAccount(Long userId, SenderAccountRequest request) {
        if (senderAccountRepository.countByUserId(userId) >= 5) {
            throw new RuntimeException("Maximum 5 sender accounts allowed");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SenderAccount account = new SenderAccount();
        account.setUser(user);
        account.setGmailAddress(request.getGmailAddress().trim().toLowerCase());
        account.setEncryptedAppPassword(encryptionUtil.encrypt(request.getAppPassword()));
        account.setLabel(request.getLabel());
        account.setActive(false);
        account = senderAccountRepository.save(account);
        return toResponse(account);
    }

    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        SenderAccount account = senderAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        senderAccountRepository.delete(account);
    }

    @Transactional
    public SenderAccountResponse setActiveAccount(Long userId, Long accountId) {
        // Deactivate all first
        senderAccountRepository.findByUserIdOrderByCreatedAtDesc(userId).forEach(a -> a.setActive(false));
        // Activate selected
        SenderAccount account = senderAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setActive(true);
        account = senderAccountRepository.save(account);
        return toResponse(account);
    }

    private SenderAccountResponse toResponse(SenderAccount a) {
        String masked = maskEmail(a.getGmailAddress());
        return SenderAccountResponse.builder()
                .id(a.getId())
                .gmailAddress(masked)
                .label(a.getLabel())
                .active(a.isActive())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(FMT) : null)
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        String shown = local.length() > 3 ? local.substring(0, 3) : local;
        return shown + "***@" + domain;
    }
}