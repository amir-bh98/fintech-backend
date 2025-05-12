package com.fintech.fintech_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fintech.fintech_backend.exception.ResourceNotFoundException;
import com.fintech.fintech_backend.mapper.CustomerMapper;
import com.fintech.fintech_backend.model.Customer;
import com.fintech.fintech_backend.model.dto.CustomerRequest;
import com.fintech.fintech_backend.model.dto.CustomerResponse;
import com.fintech.fintech_backend.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // Create
    public CustomerResponse createCustomer(CustomerRequest request) {
        Customer customer = customerMapper.mapRequestToEntity(request);
        return customerMapper.mapToResponse(customerRepository.save(customer));
    }

    // Read (single)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return customerMapper.mapToResponse(customer);
    }

    // Internal method: Returns the entity (for service-layer use)
    public Customer getCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    // Read (all)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // Update
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        existingCustomer.setName(request.getName());
        existingCustomer.setEmail(request.getEmail());
        existingCustomer.setPhone(request.getPhone());
        existingCustomer.setAddress(request.getAddress());

        return customerMapper.mapToResponse(customerRepository.save(existingCustomer));
    }

    // Delete
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

}
