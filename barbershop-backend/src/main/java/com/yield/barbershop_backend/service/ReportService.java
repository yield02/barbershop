package com.yield.barbershop_backend.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.report.OverviewRevenueDTO;
import com.yield.barbershop_backend.dto.report.CategoryRevenueDTO;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.model.Payment;
import com.yield.barbershop_backend.model.Payment.PaymentStatus;
import com.yield.barbershop_backend.specification.OrderSpecification;
import com.yield.barbershop_backend.util.DateAndTimeUltil;

import jakarta.transaction.Transactional;


@Service
public class ReportService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;
    
    @Transactional
    public OverviewRevenueDTO getDateOverviewRevenue(Date date) {

        List<Payment> payments = paymentService.getPaymentCurrentAndPreviousDates(date);
        

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        CompletableFuture<Double> successPaymentCurrentFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
               
                    if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && dateFormat.format(payment.getPaymentDate()).equals(dateFormat.format(date))) {
                        return payment.getAmount();
                    } else {
                        return 0.0;
                    }
                }).sum();   
        });

        CompletableFuture<Double> refundedPaymentCurrentFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                    if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && dateFormat.format(payment.getPaymentDate()).equals(dateFormat.format(date))) {
                        return payment.getAmount();
                    } else {
                        return 0.0;
                    }
                }).sum();
        });

        CompletableFuture<Double> successPaymentPreviousFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                    if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().before(date)) {
                        return payment.getAmount();
                    } else {
                        return 0.0;
                    }
                }).sum();
        });

        CompletableFuture<Double> refundedPaymentPreviousFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                    if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().before(date)) {
                        return payment.getAmount();
                    } else {
                        return 0.0;
                    }
                }).sum();
        });

        try {

            Double successPaymentCurrent = successPaymentCurrentFuture.get();
            Double refundedPaymentCurrent = refundedPaymentCurrentFuture.get();
            Double successPaymentPrevious = successPaymentPreviousFuture.get();
            Double refundedPaymentPrevious = refundedPaymentPreviousFuture.get();

            System.out.println("successPaymentCurrent: " + successPaymentCurrent);
            System.out.println("refundedPaymentCurrent: " + refundedPaymentCurrent);
            System.out.println("successPaymentPrevious: " + successPaymentPrevious);
            System.out.println("refundedPaymentPrevious: " + refundedPaymentPrevious);

            Double totalRevenueCurrent = successPaymentCurrent + refundedPaymentCurrent;
            Double totalRevenuePrevious = successPaymentPrevious + refundedPaymentPrevious;
            Double netRevenue = successPaymentCurrent;
            Long totalTransactions = (Long) payments.stream().count();
            Double averageTransactionAmount = successPaymentCurrent / totalTransactions;

            Double revenueGrowth = ((totalRevenueCurrent - totalRevenuePrevious) / totalRevenuePrevious) * 100;

            OverviewRevenueDTO overviewRevenueDTO = new OverviewRevenueDTO();
            overviewRevenueDTO.setTotalRevenue(totalRevenueCurrent);
            overviewRevenueDTO.setNetRevenue(netRevenue);
            overviewRevenueDTO.setTotalTransactions(totalTransactions);
            overviewRevenueDTO.setAverageTransactionAmount(averageTransactionAmount);
            overviewRevenueDTO.setRevenueGrowthPercent(Math.round(revenueGrowth * Math.pow(10, 2))
                 / Math.pow(10, 2));

            return overviewRevenueDTO;

        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public OverviewRevenueDTO getWeekOverviewRevenue(Date date) {

        List<Payment> payments = paymentService.getPaymentCurrentAndPreviousWeek(date);

        List<Payment> successPaymentCurrent = new ArrayList<>();
        List<Payment> refundedPaymentCurrent = new ArrayList<>();
        List<Payment> successPaymentPrevious = new ArrayList<>();
        List<Payment> refundedPaymentPrevious = new ArrayList<>();

        Date[] dates = new DateAndTimeUltil().getWeekStartEnd(date);
        Date startDay = dates[0];
        Date endDay = dates[1];

        payments.forEach(payment -> {
            if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();



        Double totalRevenuePrevious = successPaymentPreviousTotal + refundedPaymentPreviousTotal;

        Double totalRevenueCurrent = successPaymentCurrentTotal + refundedPaymentCurrentTotal;
        Double netRevenue = successPaymentCurrentTotal;
        Long totalTransactions = Long.valueOf(successPaymentCurrent.size() + refundedPaymentCurrent.size());
        Double averageTransactionAmount = successPaymentCurrentTotal / totalTransactions;
        
        Double revenueGrowth = ((totalRevenueCurrent - totalRevenuePrevious) / totalRevenuePrevious) * 100;


        OverviewRevenueDTO overviewRevenueDTO = new OverviewRevenueDTO();
        overviewRevenueDTO.setTotalRevenue(totalRevenueCurrent);
        overviewRevenueDTO.setNetRevenue(netRevenue);
        overviewRevenueDTO.setTotalTransactions(totalTransactions);
        overviewRevenueDTO.setAverageTransactionAmount(averageTransactionAmount);
        overviewRevenueDTO.setRevenueGrowthPercent(Math.round(revenueGrowth * Math.pow(10, 2))
                 / Math.pow(10, 2));

        return overviewRevenueDTO;
    }

    public OverviewRevenueDTO getMonthOverviewRevenue(Date date) {

        List<Payment> payments = paymentService.getPaymentCurrentAndPreviousMonth(date);


        List<Payment> successPaymentCurrent = new ArrayList<>();
        List<Payment> refundedPaymentCurrent = new ArrayList<>();
        List<Payment> successPaymentPrevious = new ArrayList<>();
        List<Payment> refundedPaymentPrevious = new ArrayList<>();

        Date[] dates = new DateAndTimeUltil().getMonthStartEnd(date);
        Date startDay = dates[0];

        payments.forEach(payment -> {
            if(payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();

        Double totalRevenueCurrent = successPaymentCurrentTotal + refundedPaymentCurrentTotal;
        Double netRevenue = successPaymentCurrentTotal;
        Long totalTransactions = Long.valueOf(successPaymentCurrent.size() + refundedPaymentCurrent.size());
        Double averageTransactionAmount = successPaymentCurrentTotal / totalTransactions;
        Double totalRevenuePrevious = successPaymentPreviousTotal + refundedPaymentPreviousTotal;

        Double revenueGrowth = ((totalRevenueCurrent - totalRevenuePrevious) / totalRevenuePrevious) * 100;

        OverviewRevenueDTO overviewRevenueDTO = new OverviewRevenueDTO();
        overviewRevenueDTO.setTotalRevenue(totalRevenueCurrent);
        overviewRevenueDTO.setNetRevenue(netRevenue);
        overviewRevenueDTO.setTotalTransactions(totalTransactions);
        overviewRevenueDTO.setAverageTransactionAmount(averageTransactionAmount);
        overviewRevenueDTO.setRevenueGrowthPercent(Math.round(revenueGrowth * Math.pow(10, 2))
                 / Math.pow(10, 2));

        return overviewRevenueDTO;
    }

    public OverviewRevenueDTO getYearOverviewRevenue(Date date) {

        List<Payment> payments = paymentService.getPaymentCurrentAndPreviousYear(date);

        List<Payment> successPaymentCurrent = new ArrayList<>();
        List<Payment> refundedPaymentCurrent = new ArrayList<>();
        List<Payment> successPaymentPrevious = new ArrayList<>();
        List<Payment> refundedPaymentPrevious = new ArrayList<>();


        Date[] dates = new DateAndTimeUltil().getYearStartEnd(date);
        Date startDay = dates[0];

        payments.forEach(payment -> {
            if(payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful) && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded) && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount).sum();


        Double totalRevenueCurrent = successPaymentCurrentTotal + refundedPaymentCurrentTotal;
        Double netRevenue = successPaymentCurrentTotal;
        Long totalTransactions = Long.valueOf(successPaymentCurrent.size() + refundedPaymentCurrent.size());
        Double averageTransactionAmount = successPaymentCurrentTotal / totalTransactions;
        Double totalRevenuePrevious = successPaymentPreviousTotal + refundedPaymentPreviousTotal;

        Double revenueGrowth = ((totalRevenueCurrent - totalRevenuePrevious) / totalRevenuePrevious) * 100;


        OverviewRevenueDTO overviewRevenueDTO = new OverviewRevenueDTO();
        overviewRevenueDTO.setTotalRevenue(totalRevenueCurrent);
        overviewRevenueDTO.setNetRevenue(netRevenue);
        overviewRevenueDTO.setTotalTransactions(totalTransactions);
        overviewRevenueDTO.setAverageTransactionAmount(averageTransactionAmount);
        overviewRevenueDTO.setRevenueGrowthPercent(Math.round(revenueGrowth * Math.pow(10, 2))
                 / Math.pow(10, 2));
        return overviewRevenueDTO;
    }



    // public CategoryRevenueDTO getRevenueByCategory(Date startDate, Date endDate) {



    //     List<Order> orders = orderService.getOrdersWithSpecification(OrderSpecification.getOrderSuccessAndDateBetween(startDate, endDate));

    //     List<OrderItem> drinkItems = orders.parallelStream()
    //         .filter(order -> order.getPayment().getStatus().equals(Payment.PaymentStatus.Successful))
    //         .flatMap(order -> order.getOrderItems().stream()).filter(item -> item.getDrinkId() != null)
    //         .collect(Collectors.toList());
    //     List<OrderItem> productItems = orders.parallelStream()
    //         .filter(order -> order.getPayment().getStatus().equals(Payment.PaymentStatus.Successful))
    //         .flatMap(order -> order.getOrderItems().stream()).filter(item -> item.getProductId() != null)
    //         .collect(Collectors.toList());
        
    //     Double netDrinkRevenue = drinkItems.parallelStream().mapToDouble(OrderItem::getPrice).sum();
    //     Double netProductRevenue = productItems.parallelStream().mapToDouble(OrderItem::getPrice).sum();
        

    //     return null;
    // }
}
