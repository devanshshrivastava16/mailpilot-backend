package com.mailpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderAccountResponse {

    private Long id;
    private String gmailAddress; // Already masked by the service layer
    private String label;
    private boolean active;
    private String createdAt;
}