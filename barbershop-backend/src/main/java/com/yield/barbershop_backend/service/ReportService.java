package com.yield.barbershop_backend.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.report.OverviewRevenueDTO;
import com.yield.barbershop_backend.dto.report.ReportRevenueByCategoryDTO.ReportType;
import com.yield.barbershop_backend.dto.report.ReportRevenueByCustomer.TotalVisit;
import com.yield.barbershop_backend.dto.report.ReportRevenueByCustomer;
import com.yield.barbershop_backend.dto.report.BarberRevenueDTO;
import com.yield.barbershop_backend.dto.report.CategoryRevenueDTO;
import com.yield.barbershop_backend.model.Appointment;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.model.Payment;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.model.Payment.PaymentStatus;
import com.yield.barbershop_backend.specification.OrderSpecification;
import com.yield.barbershop_backend.util.DateAndTimeUltil;
import com.yield.barbershop_backend.util.NumberUtil;

import jakarta.transaction.Transactional;

@Service
public class ReportService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired 
    private AppointmentService appointmentService;

    @Autowired 
    private AppointmentServiceService appointmentServiceService;

    @Autowired
    private UserService userService;


    @Transactional
    public OverviewRevenueDTO getDateOverviewRevenue(Date date) {

        List<Payment> payments = paymentService.getPaymentCurrentAndPreviousDates(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        CompletableFuture<Double> successPaymentCurrentFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {

                if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                        && dateFormat.format(payment.getPaymentDate()).equals(dateFormat.format(date))) {
                    return payment.getAmount();
                } else {
                    return 0.0;
                }
            }).sum();
        });

        CompletableFuture<Double> refundedPaymentCurrentFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                        && dateFormat.format(payment.getPaymentDate()).equals(dateFormat.format(date))) {
                    return payment.getAmount();
                } else {
                    return 0.0;
                }
            }).sum();
        });

        CompletableFuture<Double> successPaymentPreviousFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                        && payment.getPaymentDate().before(date)) {
                    return payment.getAmount();
                } else {
                    return 0.0;
                }
            }).sum();
        });

        CompletableFuture<Double> refundedPaymentPreviousFuture = CompletableFuture.supplyAsync(() -> {
            return payments.stream().mapToDouble(payment -> {
                if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                        && payment.getPaymentDate().before(date)) {
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

        } catch (InterruptedException | ExecutionException e) {
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
            if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();

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
            if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();

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
            if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().after(startDay)) {
                successPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().after(startDay)) {
                refundedPaymentCurrent.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Successful)
                    && payment.getPaymentDate().before(startDay)) {
                successPaymentPrevious.add(payment);
            } else if (payment.getStatus().equals(Payment.PaymentStatus.Refunded)
                    && payment.getPaymentDate().before(startDay)) {
                refundedPaymentPrevious.add(payment);
            }
        });

        Double successPaymentCurrentTotal = successPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentCurrentTotal = refundedPaymentCurrent.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double successPaymentPreviousTotal = successPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();
        Double refundedPaymentPreviousTotal = refundedPaymentPrevious.parallelStream().mapToDouble(Payment::getAmount)
                .sum();

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

    public List<CategoryRevenueDTO> getRevenueByCategory(Date startDate, Date endDate) {

        List<Payment> payments = paymentService.getPaymentBetweenTwoDates(startDate, endDate);

        Double totalNetRevenue = payments.parallelStream().mapToDouble(Payment::getAmount).sum();

        // At this, we can combine calculate product revenue and drink revenue because both of them have to get all orders in the same time.
        CompletableFuture<CategoryRevenueDTO> productRevenueFuture = CompletableFuture.supplyAsync(() ->
            getProductRevenue(startDate, endDate, totalNetRevenue)
        );

        
        CompletableFuture<CategoryRevenueDTO> drinkRevenueFuture = CompletableFuture.supplyAsync(() ->
            getDrinkRevenue(startDate, endDate, totalNetRevenue)
        );

        CompletableFuture<CategoryRevenueDTO> serviceRevenueFuture = CompletableFuture.supplyAsync(() ->
            getServiceRevenue(startDate, endDate, totalNetRevenue)
        );


        // Chờ tất cả kết quả
        try {
            CategoryRevenueDTO productRevenue = productRevenueFuture.get();
            CategoryRevenueDTO serviceRevenue = serviceRevenueFuture.get();
            CategoryRevenueDTO drinkRevenue = drinkRevenueFuture.get();

            List<CategoryRevenueDTO> categoryRevenueDTOS = new ArrayList<>();
            categoryRevenueDTOS.add(productRevenue);
            categoryRevenueDTOS.add(serviceRevenue);
            categoryRevenueDTOS.add(drinkRevenue);

            return categoryRevenueDTOS;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error calculating revenue by category", e);
        }
    }

    private CategoryRevenueDTO getProductRevenue(Date startDate, Date endDate, Double totalNetRevenue) {
        
        List<Order> orders = orderService.getOrdersCompletedBetweenTwoDates(startDate, endDate);
        List<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderIds(orderIds);

        List<OrderItem> orderProductItems = orderItems.stream()
                .filter(orderItem -> orderItem.getProductId() != null)
                .collect(Collectors.toList());

        Double totalProductNetRevenue = 0.0;
        Double totalProductRevenue = 0.0;

        for (OrderItem orderProductItem : orderProductItems) {
            totalProductRevenue += orderProductItem.getOriginalPrice();
            totalProductNetRevenue += orderProductItem.getFinalPrice();
        }

        CategoryRevenueDTO categoryRevenueDTO = new CategoryRevenueDTO();
        categoryRevenueDTO.setCategory("Product");
        categoryRevenueDTO.setTotalRevenue(totalProductRevenue);
        categoryRevenueDTO.setNetRevenue(totalProductNetRevenue);
        categoryRevenueDTO.setPercentageOfTotalNetRevenue(NumberUtil.round((totalProductNetRevenue / totalNetRevenue) * 100, 2));

        return categoryRevenueDTO;
    }

    private CategoryRevenueDTO getServiceRevenue(Date startDate, Date endDate, Double totalNetRevenue) {

        List<Appointment> appointments = appointmentService.getCompletedAppointmentsBetweenTwoDates(startDate, endDate);
        List<Long> appointmentIds = appointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());

        List<com.yield.barbershop_backend.model.AppointmentService> appointmentServices = appointmentServiceService.getAllAppointmentsByAppointmentIds(appointmentIds);

        Double totalServiceNetRevenue = 0.0;
        Double totalServiceRevenue = 0.0;

        for (com.yield.barbershop_backend.model.AppointmentService appointmentService : appointmentServices) {
            totalServiceRevenue += appointmentService.getOriginalPrice();
            totalServiceNetRevenue += appointmentService.getFinalPrice();
        }

        CategoryRevenueDTO categoryRevenueDTO = new CategoryRevenueDTO();
        categoryRevenueDTO.setCategory("Service");
        categoryRevenueDTO.setTotalRevenue(totalServiceRevenue);
        categoryRevenueDTO.setNetRevenue(totalServiceNetRevenue);
        categoryRevenueDTO.setPercentageOfTotalNetRevenue(NumberUtil.round((totalServiceNetRevenue / totalNetRevenue) * 100, 2));
        return categoryRevenueDTO;
    }

    private CategoryRevenueDTO getDrinkRevenue(Date startDate, Date endDate, Double totalNetRevenue) {
        
        List<Order> orders = orderService.getOrdersCompletedBetweenTwoDates(startDate, endDate);
        List<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderIds(orderIds);

        List<OrderItem> orderDrinkItems = orderItems.stream()
                .filter(orderItem -> orderItem.getDrinkId() != null)
                .collect(Collectors.toList());

        Double totalDrinkNetRevenue = 0.0;
        Double totalDrinkRevenue = 0.0;

        for (OrderItem orderDrinkItem : orderDrinkItems) {
            totalDrinkRevenue += orderDrinkItem.getOriginalPrice();
            totalDrinkNetRevenue += orderDrinkItem.getFinalPrice();
        }

        CategoryRevenueDTO categoryRevenueDTO = new CategoryRevenueDTO();
        categoryRevenueDTO.setCategory("Drink");
        categoryRevenueDTO.setTotalRevenue(totalDrinkRevenue);
        categoryRevenueDTO.setNetRevenue(totalDrinkNetRevenue);
        categoryRevenueDTO.setPercentageOfTotalNetRevenue(NumberUtil.round((totalDrinkNetRevenue / totalNetRevenue) * 100, 2));
        
        return categoryRevenueDTO;
    }

    public List<BarberRevenueDTO> getRevenueByBarber(Date startDateObj, Date endDateObj) {

        List<BarberRevenueDTO> barberRevenueDTOS = new ArrayList<>();

        List<Appointment> appointments = appointmentService.getCompletedAppointmentsBetweenTwoDatesByStartTime(startDateObj, endDateObj);
        // <userId, List<Appointment>>
        Map<Long, List<Appointment>> barberAppointments = appointments.stream().collect(Collectors.groupingBy(Appointment::getUserId));

        Double totalNetRevenue = appointments.parallelStream().mapToDouble(Appointment::getTotalAmount).sum();

        Set<Long> userIds = appointments.stream().map(Appointment::getUserId).collect(Collectors.toSet());
        
        Map<Long, User> users = userService.getUserByIds(userIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));

        for (Long userId : userIds) {
            List<Appointment> userAppointments = barberAppointments.get(userId);

            Double totalUserNetRevenue = userAppointments.parallelStream().mapToDouble(Appointment::getTotalAmount).sum();
            User user = users.get(userId);
            
            if(user != null) {
                BarberRevenueDTO barberRevenueDTO = new BarberRevenueDTO();
                barberRevenueDTO.setUserId(userId);
                barberRevenueDTO.setFullName(user.getFullName());
                barberRevenueDTO.setRole(user.getRole());
                barberRevenueDTO.setTotalNetRevenue(totalUserNetRevenue);
                barberRevenueDTO.setTotalAppointment(userAppointments.size());
                barberRevenueDTO.setPercentageOfTotalNetRevenue(NumberUtil.round((totalUserNetRevenue / totalNetRevenue) * 100, 2));
                barberRevenueDTOS.add(barberRevenueDTO);
            }
        }


        return barberRevenueDTOS;
    }

    public List<ReportRevenueByCustomer> getRevenueByCustomers(Date startDateObj, Date endDateObj) {

        List<Order> orders = orderService.getOrdersCompletedBetweenTwoDates(startDateObj, endDateObj);
        List<Appointment> appointments = appointmentService.getCompletedAppointmentsBetweenTwoDates(startDateObj, endDateObj);

        // <customerId, Order>
        Map<Long, List<Order>> orderMapByCustomerId = orders.stream().collect(Collectors.groupingBy(Order::getCustomerId));
        // <customerId, Appointment>
        Map<Long, List<Appointment>> appointmentMapByCustomerId = appointments.stream().collect(Collectors.groupingBy(Appointment::getCustomerId));
        Set<Long> customerIdsSet = new HashSet<>();
        customerIdsSet.addAll(orderMapByCustomerId.keySet());
        customerIdsSet.addAll(appointmentMapByCustomerId.keySet());

        Map<Long, User> users = userService.getUserByIds(customerIdsSet).stream().collect(Collectors.toMap(User::getUserId, user -> user));

        List<ReportRevenueByCustomer> reportRevenueByCustomers = new ArrayList<>();

        for (Long customerId : customerIdsSet) {
            User user = users.get(customerId);
            List<Appointment> appointmentsByCustomerId = appointmentMapByCustomerId.get(customerId);
            List<Order> ordersByCustomerId = orderMapByCustomerId.get(customerId);
            
            Double totalNetRevenueOfAppointments = appointmentsByCustomerId != null ? appointmentsByCustomerId.stream().mapToDouble(Appointment::getTotalAmount).sum() : 0.0;
            Double totalNetRevenueOfOrders = ordersByCustomerId != null ? ordersByCustomerId.stream().mapToDouble(Order::getTotalAmount).sum() : 0.0;
            Double totalNetRevenue = totalNetRevenueOfAppointments + totalNetRevenueOfOrders;

            Long totalAppointments = appointmentsByCustomerId != null ? appointmentsByCustomerId.size() * 1L : 0L;
            Long totalOrders = ordersByCustomerId != null ? ordersByCustomerId.size() * 1L : 0L;

            if (user != null) {
                ReportRevenueByCustomer reportRevenueByCustomer = new ReportRevenueByCustomer();
                reportRevenueByCustomer.setCustomerId(customerId);
                reportRevenueByCustomer.setCustomerName(user.getFullName());
                reportRevenueByCustomer.setCustomerPhone(user.getPhoneNumber());
                reportRevenueByCustomer.setCustomerEmail(user.getEmail());
                reportRevenueByCustomer.setTotalNetRevenue(totalNetRevenue);
                reportRevenueByCustomer.setTotalVisit(new TotalVisit(totalAppointments, totalOrders));
                reportRevenueByCustomers.add(reportRevenueByCustomer);
            }
        }
        return reportRevenueByCustomers;
    }

}
