package com.fintech.fintech_backend.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerRequest {
    @NotBlank
    private String name;
    @Email
    private String email;
    private String phone;
    private String address;
}
