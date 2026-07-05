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
public class RecipientDetailResponse {

    private String recipientName;
    private String recipientEmail;
    private String companyName;
    private String status;
    private String errorMessage;
    private LocalDateTime sentAt;
}