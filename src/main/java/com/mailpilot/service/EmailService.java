package com.mailpilot.service;

import com.mailpilot.dto.*;
import com.mailpilot.entity.*;
import com.mailpilot.repository.*;
import com.mailpilot.util.EncryptionUtil;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SenderAccountRepository senderAccountRepository;
    private final EmailCampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final EncryptionUtil encryptionUtil;

    public List<CampaignPreviewItem> generatePreview(Long userId, EmailCampaignRequest request) {
        validateRecipients(request);
        List<CampaignPreviewItem> items = new ArrayList<>();
        for (int i = 0; i < request.getRecipientNames().size(); i++) {
            String html = replacePlaceholders(
                    request.getHtmlTemplate(),
                    request.getRecipientNames().get(i),
                    request.getCompanyNames().get(i),
                    request.getRecipientEmails().get(i)
            );
            items.add(CampaignPreviewItem.builder()
                    .serialNumber(i + 1)
                    .recipientName(request.getRecipientNames().get(i).trim())
                    .recipientEmail(request.getRecipientEmails().get(i).trim())
                    .companyName(request.getCompanyNames().get(i).trim())
                    .previewHtml(html)
                    .build());
        }
        return items;
    }

    @Transactional
    public CampaignResponse sendCampaign(Long userId, EmailCampaignRequest request, MultipartFile resumeFile) {
        validateRecipients(request);

        SenderAccount senderAccount = senderAccountRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No active sender account selected. Please select one."));

        // Read resume bytes once
        byte[] resumeBytes = null;
        String resumeContentType = null;
        String resumeFileName = null;

        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                resumeBytes = resumeFile.getBytes();
                resumeFileName = resumeFile.getOriginalFilename();
                resumeContentType = resumeFileName.endsWith(".docx")
                        ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        : "application/pdf";
                log.info("Resume loaded: {} bytes, filename: {}, type: {}", resumeBytes.length, resumeFileName, resumeContentType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read resume file: " + e.getMessage());
            }
        }

        // Save campaign
        User userRef = senderAccount.getUser();
        EmailCampaign campaign = new EmailCampaign();
        campaign.setUser(userRef);
        campaign.setSenderAccount(senderAccount);
        campaign.setSubject(request.getSubject().trim());
        campaign.setHtmlTemplate(request.getHtmlTemplate());
        campaign.setResumeUrl(resumeFileName); // store filename for reference
        campaign = campaignRepository.save(campaign);

        // Save recipients as PENDING
        List<CampaignRecipient> recipients = new ArrayList<>();
        for (int i = 0; i < request.getRecipientNames().size(); i++) {
            CampaignRecipient r = new CampaignRecipient();
            r.setCampaign(campaign);
            r.setRecipientName(request.getRecipientNames().get(i).trim());
            r.setRecipientEmail(request.getRecipientEmails().get(i).trim());
            r.setCompanyName(request.getCompanyNames().get(i).trim());
            r.setStatus("PENDING");
            recipients.add(r);
        }
        recipientRepository.saveAll(recipients);

        // Send emails one by one
        String decryptedPassword = encryptionUtil.decrypt(senderAccount.getEncryptedAppPassword());
        for (CampaignRecipient r : recipients) {
            try {
                sendSingleEmail(senderAccount.getGmailAddress(), decryptedPassword,
                        r.getRecipientEmail(), request.getSubject(),
                        replacePlaceholders(request.getHtmlTemplate(), r.getRecipientName(), r.getCompanyName(), r.getRecipientEmail()),
                        resumeBytes, resumeFileName, resumeContentType);
                r.setStatus("SENT");
                r.setSentAt(LocalDateTime.now());
                r.setErrorMessage(null);
            } catch (Exception e) {
                log.error("Failed to send to {}: {}", r.getRecipientEmail(), e.getMessage());
                r.setStatus("FAILED");
                r.setErrorMessage(e.getMessage());
            }
        }
        recipientRepository.saveAll(recipients);

        long total = recipients.size();
        long sent = recipients.stream().filter(r -> "SENT".equals(r.getStatus())).count();
        long failed = total - sent;

        return CampaignResponse.builder()
                .id(campaign.getId())
                .senderEmail(senderAccount.getGmailAddress())
                .subject(campaign.getSubject())
                .createdAt(campaign.getCreatedAt())
                .totalRecipients((int) total)
                .successfulSends((int) sent)
                .failedSends((int) failed)
                .build();
    }

    public List<CampaignResponse> getCampaigns(Long userId) {
        return campaignRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(c -> {
                    long total = recipientRepository.countByCampaignIdAndStatus(c.getId(), "SENT")
                            + recipientRepository.countByCampaignIdAndStatus(c.getId(), "FAILED")
                            + recipientRepository.countByCampaignIdAndStatus(c.getId(), "PENDING");
                    long sent = recipientRepository.countByCampaignIdAndStatus(c.getId(), "SENT");
                    long failed = recipientRepository.countByCampaignIdAndStatus(c.getId(), "FAILED");
                    return CampaignResponse.builder()
                            .id(c.getId())
                            .senderEmail(c.getSenderAccount() != null ? c.getSenderAccount().getGmailAddress() : "N/A")
                            .subject(c.getSubject())
                            .createdAt(c.getCreatedAt())
                            .totalRecipients((int) total)
                            .successfulSends((int) sent)
                            .failedSends((int) failed)
                            .build();
                }).toList();
    }

    public CampaignDetailResponse getCampaignDetail(Long userId, Long campaignId) {
        EmailCampaign campaign = campaignRepository.findByIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        List<RecipientDetailResponse> recipientDetails = recipientRepository
                .findByCampaignIdOrderByRecipientName(campaignId).stream()
                .map(r -> RecipientDetailResponse.builder()
                        .recipientName(r.getRecipientName())
                        .recipientEmail(r.getRecipientEmail())
                        .companyName(r.getCompanyName())
                        .status(r.getStatus())
                        .errorMessage(r.getErrorMessage())
                        .sentAt(r.getSentAt())
                        .build())
                .toList();

        long sent = recipientDetails.stream().filter(d -> "SENT".equals(d.getStatus())).count();
        long failed = recipientDetails.stream().filter(d -> "FAILED".equals(d.getStatus())).count();

        CampaignResponse base = CampaignResponse.builder()
                .id(campaign.getId())
                .senderEmail(campaign.getSenderAccount() != null ? campaign.getSenderAccount().getGmailAddress() : "N/A")
                .subject(campaign.getSubject())
                .createdAt(campaign.getCreatedAt())
                .totalRecipients(recipientDetails.size())
                .successfulSends((int) sent)
                .failedSends((int) failed)
                .build();
        return CampaignDetailResponse.from(base, recipientDetails);
    }

    private void validateRecipients(EmailCampaignRequest request) {
        int nameSize = request.getRecipientNames().size();
        int emailSize = request.getRecipientEmails().size();
        int companySize = request.getCompanyNames().size();
        if (nameSize != emailSize || nameSize != companySize) {
            throw new RuntimeException("Names, emails, and company names lists must have the same number of entries");
        }
        if (nameSize == 0) {
            throw new RuntimeException("At least one recipient is required");
        }
        if (nameSize > 100) {
            throw new RuntimeException("Maximum 100 recipients allowed per campaign");
        }
        for (String email : request.getRecipientEmails()) {
            if (email == null || !email.trim().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                throw new RuntimeException("Invalid email address: " + email);
            }
        }
    }

    private void sendSingleEmail(String from, String password, String to, String subject,
                             String htmlBody, byte[] resumeBytes, String resumeFileName, String resumeContentType) throws Exception {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(465);                    // ← changed from 587
    mailSender.setProtocol("smtps");            // ← changed from smtp
    mailSender.setUsername(from);
    mailSender.setPassword(password);
    mailSender.setDefaultEncoding("UTF-8");

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.ssl.enable", "true");  // ← changed from starttls
    props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    props.put("mail.smtp.connectiontimeout", "15000");
    props.put("mail.smtp.timeout", "15000");
    props.put("mail.smtp.writetimeout", "15000");

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);

    if (resumeBytes != null && resumeBytes.length > 0) {
        helper.addAttachment(resumeFileName, new ByteArrayDataSource(resumeBytes, resumeContentType));
    }

    mailSender.send(message);
}

    private String replacePlaceholders(String template, String name, String company, String email) {
        return template
                .replace("{{name}}", name != null ? name : "")
                .replace("{{company}}", company != null ? company : "")
                .replace("{{email}}", email != null ? email : "");
    }

}