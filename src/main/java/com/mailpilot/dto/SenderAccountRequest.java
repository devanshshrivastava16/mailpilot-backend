package com.mailpilot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderAccountRequest {

    @NotBlank(message = "Gmail address is required")
    @Email(message = "Gmail address must be a valid email address")
    private String gmailAddress;

    @NotBlank(message = "App password is required")
    private String appPassword;

    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;
}