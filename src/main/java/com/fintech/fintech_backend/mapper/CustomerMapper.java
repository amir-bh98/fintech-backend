package com.fintech.fintech_backend.mapper;

import org.springframework.stereotype.Component;

import com.fintech.fintech_backend.model.Customer;
import com.fintech.fintech_backend.model.dto.CustomerRequest;
import com.fintech.fintech_backend.model.dto.CustomerResponse;

@Component
public class CustomerMapper {

    public Customer mapRequestToEntity(CustomerRequest request) {
        return Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
    }

    public Customer mapResponseToEntity(CustomerResponse response) {
        return Customer.builder()
                .id(response.getId())
                .name(response.getName())
                .email(response.getEmail())
                .phone(response.getPhone())
                .address(response.getAddress())
                .createdAt(response.getCreatedAt())
                .build();
    }

    public CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
