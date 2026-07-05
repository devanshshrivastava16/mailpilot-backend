package com.mailpilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCampaignRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @NotBlank(message = "HTML template is required")
    private String htmlTemplate;

    @NotEmpty(message = "Recipient names must not be empty")
    @Size(max = 100, message = "Recipient names list must not exceed 100 entries")
    private List<String> recipientNames;

    @NotEmpty(message = "Recipient emails must not be empty")
    @Size(max = 100, message = "Recipient emails list must not exceed 100 entries")
    private List<String> recipientEmails;

    @NotEmpty(message = "Company names must not be empty")
    @Size(max = 100, message = "Company names list must not exceed 100 entries")
    private List<String> companyNames;
}