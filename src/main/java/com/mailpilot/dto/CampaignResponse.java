package com.mailpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private Long id;
    private String senderEmail;
    private String subject;
    private LocalDateTime createdAt;
    private int totalRecipients;
    private int successfulSends;
    private int failedSends;
}