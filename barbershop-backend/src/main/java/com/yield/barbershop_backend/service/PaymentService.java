package com.yield.barbershop_backend.service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.model.Payment;
import com.yield.barbershop_backend.repository.PaymentRepo;
import com.yield.barbershop_backend.specification.PaymentSpecification;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    public List<Payment> getPaymentCurrentAndPreviousDates(Date date) {
        return paymentRepo.findAll(PaymentSpecification.getPaymentCurrentAndPreviousDates(date));
    }

    public List<Payment> getPaymentCurrentAndPreviousWeek(Date date) {
        return paymentRepo.findAll(PaymentSpecification.getPaymentCurrentAndPreviousWeek(date));
    }

    public List<Payment> getPaymentCurrentAndPreviousMonth(Date date) {
        return paymentRepo.findAll(PaymentSpecification.getPaymentCurrentAndPreviousMonth(date));
    }

    public List<Payment> getPaymentCurrentAndPreviousYear(Date date) {
        return paymentRepo.findAll(PaymentSpecification.getPaymentCurrentAndPreviousYear(date));
    }

}

