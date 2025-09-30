package com.yield.barbershop_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.customer.CustomerFilterDTO;
import com.yield.barbershop_backend.dto.customer.CustomerRegisterDTO;
import com.yield.barbershop_backend.dto.customer.CustomerUpdateDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.CustomerVerification;
import com.yield.barbershop_backend.model.UserVerification;
import com.yield.barbershop_backend.repository.CustomerRepo;
import com.yield.barbershop_backend.repository.CustomerVerificationRepo;
import com.yield.barbershop_backend.specification.CustomerSpecfication;
import com.yield.barbershop_backend.util.EmailUltil;

@Service
public class CustomerService {
    

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private CustomerVerificationRepo customerVerificationRepo;

    @Autowired
    private EmailUltil emailUltil;

    @Autowired
    private EmailService emailService;

    public UserDetails loadCustomerByEmail(String email) {
        Customer customer = customerRepo.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        return new com.yield.barbershop_backend.model.AccountPrincipal<Customer>(customer);
    }


    public Customer getCustomerById(Long customerId) {
        return customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));
    }

    public Page<Customer> getCustomersByFilter(CustomerFilterDTO filter) {
        Pageable pageable = PageRequest.ofSize(filter.getPageSize()).withPage(filter.getPage());
        return customerRepo.findAll(CustomerSpecfication.filters(filter), pageable);
    }

    public Customer createCustomer(CustomerRegisterDTO customer) {

        if (!customerRepo.findByEmailOrPhoneNumber(customer.getEmail(), customer.getPhoneNumber()).isEmpty()) {
            throw new DataConflictException("Email or phone number already in use");
        }

        Customer newCustomer = new Customer();
        newCustomer.setFullName(customer.getFullName());
        newCustomer.setPhoneNumber(customer.getPhoneNumber());
        newCustomer.setEmail(customer.getEmail());
        // TODO: Hash the password before saving
        newCustomer.setPassword(customer.getPassword());
        
        return customerRepo.save(newCustomer);
    }

    public void updateCustomer(Long customerId, CustomerUpdateDTO customer) {
    
        Customer existingCustomer = customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(!customerRepo.findByEmailOrPhoneNumber(customer.getEmail(), customer.getPhoneNumber()).isEmpty()) {
            throw new DataConflictException("Email or phone number already in use");
        }

        existingCustomer.setFullName(customer.getFullName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhoneNumber(customer.getPhoneNumber());
        existingCustomer.setAddress(customer.getAddress());
        existingCustomer.setNotes(customer.getNotes());
        customerRepo.save(existingCustomer);
    }


    public void changePassword(Long customerId, String newPassword) {
        Customer existingCustomer = customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(existingCustomer.getPassword().equals(newPassword)) {
            throw new DataConflictException("New password must be different from the old password");
        }
        
        existingCustomer.setPassword(newPassword);
        customerRepo.save(existingCustomer);
    }

    @Transactional
    public void sendCustomerEmailVerification(Long customerId) {


        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(customer.getEmail() == null) {
            throw new DataNotFoundException("Email not found");
        }


        CustomerVerification customerVerification = customerVerificationRepo.findByCustomerIdAndType(customerId, CustomerVerification.VerificationType.EMAIL);


        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);


        if(customerVerification != null) {

            if(customerVerification.getVerified() == true) {
                throw new DataNotFoundException("Email already verified");
            }
            else if(customerVerification.getExpiry_at().isAfter(LocalDateTime.now())) {
                throw new DataNotFoundException("Verification email already sent");
            }

        }

        else {
            customerVerification = new CustomerVerification();
            customerVerification.setCustomerId(customerId);
            customerVerification.setType(CustomerVerification.VerificationType.EMAIL);
            customerVerification.setVerified(false);
        }

        customerVerification.setToken_hash(token);
        customerVerification.setExpiry_at(expiryTime);
        customerVerificationRepo.save(customerVerification);
        
        String verificationLink = emailUltil.getAppBaseUrl() + "/auth/customer/verify-email?token=" + token + "&customerId=" + customerId;
        String text = emailUltil.getCustomerVerificationTextHtml(customer.getFullName(), verificationLink, expiryTime);

        emailService.sendEmail(customer.getEmail(), "[BaberShop] Verify your email", text);
    }


    public void verifyCustomerEmail(Long customerId, String token) {

        CustomerVerification customerVerification = customerVerificationRepo.findByCustomerIdAndType(customerId, CustomerVerification.VerificationType.EMAIL);
        
        if(customerVerification.getExpiry_at().isBefore(LocalDateTime.now())) {
            customerVerificationRepo.delete(customerVerification);
            throw new DataNotFoundException("Verification token has expired");
        }

        if(customerVerification.getToken_hash().equals(token)) {
            customerVerification.setVerified(true);
            customerVerification.setVerified_at(LocalDateTime.now());
            customerVerificationRepo.save(customerVerification);
        }
        else {
            customerVerificationRepo.delete(customerVerification);
            throw new DataNotFoundException("Verification token is incorrect");
        }

    }




}
