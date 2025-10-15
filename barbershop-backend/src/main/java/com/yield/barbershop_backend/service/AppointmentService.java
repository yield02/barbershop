package com.yield.barbershop_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.appointment.AppointmentFilterDTO;
import com.yield.barbershop_backend.dto.appointment.CreateAppointmentDTO;
import com.yield.barbershop_backend.dto.appointment.UpdateAppointmentDTO;
import com.yield.barbershop_backend.dto.appointment.UpdateStatusAppointmentDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Appointment;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.model.PromotionItem;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.repository.AppointmentRepo;
import com.yield.barbershop_backend.specification.AppointmentSpecification;


@Service
public class  AppointmentService {
    

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private AppointmentServiceService appointmentServiceService;
    
    @Autowired
    private PromotionService promotionService;

    // @Autowired
    // private AppointmentServiceRepo appointmentServiceRepo;

    public Page<Appointment> getAppointmentsWithFilter(AppointmentFilterDTO filters) {

        Pageable page = PageRequest.of(filters.getPage(), filters.getPageSize());

        return appointmentRepo.findAll(AppointmentSpecification.filters(filters), page);
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Appointment not found"));
    }

    public void deleteAppointment(Long appointmentId) {
        appointmentRepo.deleteById(appointmentId);
    }

    @Transactional
    public Appointment createAppointment(CreateAppointmentDTO appointment) throws DataNotFoundException, DataConflictException {
        // Check and get user
        User user = userService.getUserById(appointment.getUserId());
        
        // Check services
        List<com.yield.barbershop_backend.model.Service> services = serviceService.findExistedServiceIds(appointment.getServiceIds());
        
        if (!services.isEmpty() && (services.size() != appointment.getServiceIds().size())) {

            List<Long> nonExistedIds = appointment.getServiceIds().stream()
                .filter(id -> !services.stream().map(com.yield.barbershop_backend.model.Service::getServiceId).collect(Collectors.toSet()).contains(id))
                .toList();

            throw new DataNotFoundException("Services not found with ids: " + nonExistedIds);
        }

        // Caculate endTime
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(
            services.stream()
                .map(com.yield.barbershop_backend.model.Service::getDurationMinutes)
                .reduce(0, Integer::sum)
        );

        // <ServiceId, PromotionItem>

        Set<PromotionItem> listPromotionItems = services.stream().flatMap(service -> {
            return service.getPromotionItems().stream();
        }).collect(Collectors.toSet());

        // serviceId, PromotionItemList

        Map<Long, List<PromotionItem>> promotionItemsByServiceId = listPromotionItems.stream().collect(Collectors.groupingBy(PromotionItem::getServiceId));

        Set<Long> dbPromotionIds = listPromotionItems.stream().map(PromotionItem::getPromotionId).collect(Collectors.toSet());

        Map<Long, Promotion> activePromotions = promotionService.getActivePromotionsByIds(dbPromotionIds.stream().toList()).stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

        Double totalDiscountAmount = services.stream().mapToDouble(service -> {
            List<PromotionItem> promotionItems = promotionItemsByServiceId.get(service.getServiceId());
            
            if(promotionItems == null || promotionItems.size() == 0) return 0.0;

         PromotionItem bestPromotionItem = promotionItems.size() == 1 ? promotionItems.get(0) : promotionItems.stream().max((PromotionItem p1, PromotionItem p2) -> {
                Promotion promotion1 = activePromotions.get(p1.getPromotionId());
                Promotion promotion2 = activePromotions.get(p2.getPromotionId());
                if (promotion1.getDiscountAmount() != null && 
                    promotion2.getDiscountAmount() != null && 
                    promotion1.getDiscountAmount() >= promotion2.getDiscountAmount()) return 1;
                else if(promotion1.getDiscountPercentage() != null && 
                        promotion2.getDiscountPercentage() != null && 
                        promotion1.getDiscountPercentage() >= promotion2.getDiscountPercentage()) return 1;
                return -1;
            }).orElse(null);

            if (bestPromotionItem == null) return 0.0;

            Promotion promotion = activePromotions.get(bestPromotionItem.getPromotionId());
            if (promotion != null) {
                if (promotion.getDiscountAmount() != null) {
                    return promotion.getDiscountAmount();
                }

                if (promotion.getDiscountPercentage() != null) {
                    return service.getPrice() * promotion.getDiscountPercentage() / 100;
                }
            }

            return 0.0;
        }).sum();

        //Caculate totalAmount
        Double totalOriginalPrice = services.stream()
            .map(com.yield.barbershop_backend.model.Service::getPrice)
            .reduce(0.0, Double::sum);

        Double totalFinalPrice = totalOriginalPrice - totalDiscountAmount;



        // Get CustomerId By Id, Se bo sung chuc nang nay khi da them jwt
        Long customerId = 1L;

        Customer customer = customerService.getCustomerById(customerId);

        // Check for appointment conflicts
        checkAppointmentConflict(appointment.getStartTime(), endTime, user.getUserId());

        // Create Appointment
        Appointment newAppointment = new Appointment();
 
        newAppointment.setCustomerName(customer.getFullName()); 
        newAppointment.setCustomerPhone(customer.getPhoneNumber());
        newAppointment.setCustomerEmail(customer.getEmail());
        newAppointment.setCustomerId(customer.getCustomerId());

        newAppointment.setUserId(user.getUserId());

        newAppointment.setTotalAmount(totalFinalPrice);
        newAppointment.setStartTime(appointment.getStartTime());
        newAppointment.setEndTime(endTime);
        newAppointment.setStatus("Pending");
        newAppointment.setNotes(appointment.getNotes());
        newAppointment.setCreatedAt(appointment.getCreatedAt());
        newAppointment.setUpdatedAt(appointment.getUpdatedAt());
        Appointment savedAppointment = appointmentRepo.save(newAppointment);


        // add appointmentservices
        List<com.yield.barbershop_backend.model.AppointmentService> appointmentServices = services.stream().map(service -> {
            
            Double originalPrice = service.getPrice();
            Double discountAmount = 0.0;
            Double finalPrice = originalPrice;

            PromotionItem promotionItem = listPromotionItems.stream().filter(item -> item.getServiceId() == service.getServiceId()).findFirst().orElse(null);

            if(promotionItem != null) {
                Promotion promotion = activePromotions.get(promotionItem.getPromotionId());
                if(promotion != null) {
                    if(promotion.getDiscountAmount() != null) {
                        discountAmount = promotion.getDiscountAmount();
                    }

                    if(promotion.getDiscountPercentage() != null) {
                        discountAmount = service.getPrice() * promotion.getDiscountPercentage() / 100;
                    }

                    finalPrice = originalPrice - discountAmount;
                }
            }
            
            com.yield.barbershop_backend.model.AppointmentService appointmentService = new com.yield.barbershop_backend.model.AppointmentService();
            appointmentService.setAppointmentId(savedAppointment.getAppointmentId());
            appointmentService.setServiceId(service.getServiceId());
            appointmentService.setServiceName(service.getServiceName());
            appointmentService.setOriginalPrice(originalPrice);
            appointmentService.setDiscountAmount(discountAmount);
            appointmentService.setFinalPrice(finalPrice);
            return appointmentService;
        }).collect(Collectors.toList());

        appointmentServiceService.createAppointmentServices(appointmentServices);

        return savedAppointment;
    }


    @Transactional
    public Appointment updateAppointment(Long appointmentId, UpdateAppointmentDTO appointment) {

        //Check appointment exsited

        Appointment existingAppointment = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Appointment not found with id: " + appointmentId));

        // Check status appointment

        if (existingAppointment.getStatus().equals("Cancelled") || existingAppointment.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot update appointment with status: " + existingAppointment.getStatus());
        }

        // Check and get user
        User user = userService.getUserById(appointment.getUserId());
        
        // Check services
        List<com.yield.barbershop_backend.model.Service> services = serviceService.findExistedServiceIds(appointment.getServiceIds());
        if (!services.isEmpty() && (services.size() != appointment.getServiceIds().size())) {

            List<Long> nonExistedIds = appointment.getServiceIds().stream()
                .filter(id -> !services.stream().map(com.yield.barbershop_backend.model.Service::getServiceId).collect(Collectors.toSet()).contains(id))
                .toList();

            throw new DataNotFoundException("Services not found with ids: " + nonExistedIds);
        }

        // Caculate endTime
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(
            services.stream()
                .map(com.yield.barbershop_backend.model.Service::getDurationMinutes)
                .reduce(0, Integer::sum)
        );

        //Caculate totalAmount
        // <ServiceId, PromotionItem>
        Set<PromotionItem> listPromotionItems = services.stream().flatMap(service -> {
            return service.getPromotionItems().stream();
        }).collect(Collectors.toSet());

        // serviceId, PromotionItemList

        Map<Long, List<PromotionItem>> promotionItemsByServiceId = listPromotionItems.stream().collect(Collectors.groupingBy(PromotionItem::getServiceId));

        Set<Long> dbPromotionIds = listPromotionItems.stream().map(PromotionItem::getPromotionId).collect(Collectors.toSet());

        Map<Long, Promotion> activePromotions = promotionService.getActivePromotionsByIds(dbPromotionIds.stream().toList()).stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

        Double totalDiscountAmount = services.stream().mapToDouble(service -> {
            List<PromotionItem> promotionItems = promotionItemsByServiceId.get(service.getServiceId());
            
            if(promotionItems == null || promotionItems.size() == 0) return 0.0;

            PromotionItem bestPromotionItem = promotionItems.size() == 1 ? promotionItems.get(0) : promotionItems.stream().max((PromotionItem p1, PromotionItem p2) -> {
                Promotion promotion1 = activePromotions.get(p1.getPromotionId());
                Promotion promotion2 = activePromotions.get(p2.getPromotionId());
                if (promotion1.getDiscountAmount() != null && 
                    promotion2.getDiscountAmount() != null && 
                    promotion1.getDiscountAmount() >= promotion2.getDiscountAmount()) return 1;
                else if(promotion1.getDiscountPercentage() != null && 
                        promotion2.getDiscountPercentage() != null && 
                        promotion1.getDiscountPercentage() >= promotion2.getDiscountPercentage()) return 1;
                return -1;
            }).orElse(null);

            if (bestPromotionItem == null) return 0.0;

            Promotion promotion = activePromotions.get(bestPromotionItem.getPromotionId());
            if (promotion != null) {
                if (promotion.getDiscountAmount() != null) {
                    return promotion.getDiscountAmount();
                }

                if (promotion.getDiscountPercentage() != null) {
                    return service.getPrice() * promotion.getDiscountPercentage() / 100;
                }
            }

            return 0.0;
        }).sum();

        //Caculate totalAmount
        Double totalOriginalPrice = services.stream()
            .map(com.yield.barbershop_backend.model.Service::getPrice)
            .reduce(0.0, Double::sum);

        Double totalFinalPrice = totalOriginalPrice - totalDiscountAmount;



        // Get CustomerId By Id, Se bo sung chuc nang nay khi da them jwt
        Long customerId = 1L;

        Customer customer = customerService.getCustomerById(customerId);

        // Check for appointment conflicts
        checkAppointmentConflictExceptCurrent(appointment.getStartTime(), endTime, user.getUserId(), appointmentId);


        // update Appointment

        existingAppointment.setUserId(user.getUserId());
        existingAppointment.setTotalAmount(totalFinalPrice);
        existingAppointment.setStartTime(appointment.getStartTime());
        existingAppointment.setEndTime(endTime);
        existingAppointment.setNotes(appointment.getNotes());
        existingAppointment.setCreatedAt(appointment.getCreatedAt());
        existingAppointment.setUpdatedAt(appointment.getUpdatedAt());
        Appointment savedAppointment = appointmentRepo.save(existingAppointment);

        // delete old services

        appointmentServiceService.deleteAppointmentServicesByAppointmentId(appointmentId);

        // add new appointmentservices
        List<com.yield.barbershop_backend.model.AppointmentService> appointmentServices = services.stream().map(service -> {
            
            Double originalPrice = service.getPrice();
            Double discountAmount = 0.0;
            Double finalPrice = originalPrice;

            PromotionItem promotionItem = listPromotionItems.stream().filter(item -> item.getServiceId() == service.getServiceId()).findFirst().orElse(null);

            if(promotionItem != null) {
                Promotion promotion = activePromotions.get(promotionItem.getPromotionId());
                if(promotion != null) {
                    if(promotion.getDiscountAmount() != null) {
                        discountAmount = promotion.getDiscountAmount();
                    }

                    if(promotion.getDiscountPercentage() != null) {
                        discountAmount = service.getPrice() * promotion.getDiscountPercentage() / 100;
                    }

                    finalPrice = originalPrice - discountAmount;
                }
            }
            
            com.yield.barbershop_backend.model.AppointmentService appointmentService = new com.yield.barbershop_backend.model.AppointmentService();
            appointmentService.setAppointmentId(savedAppointment.getAppointmentId());
            appointmentService.setServiceId(service.getServiceId());
            appointmentService.setServiceName(service.getServiceName());
            appointmentService.setOriginalPrice(originalPrice);
            appointmentService.setDiscountAmount(discountAmount);
            appointmentService.setFinalPrice(finalPrice);
            return appointmentService;
        }).collect(Collectors.toList());

        appointmentServiceService.createAppointmentServices(appointmentServices);

        return savedAppointment;
    }


    public List<Appointment> checkAppointmentsConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        return appointmentRepo.findAll(AppointmentSpecification.checkAppointmentsConflict(startTime, endTime, userId));
    }


    public void checkAppointmentConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);
        if(!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

    public void checkAppointmentConflictExceptCurrent(LocalDateTime startTime, LocalDateTime endTime, Long userId, Long currentAppointmentId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);

        conflictingAppointments.removeIf(appointment -> appointment.getAppointmentId().equals(currentAppointmentId));

        if(!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

    public void updateStatusAppointment(Long appointmentId, UpdateStatusAppointmentDTO status) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus().equals("Cancelled") || appointment.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot update status of appointment with status: " + appointment.getStatus());
        }
        appointment.setStatus(status.getStatus());
        appointmentRepo.save(appointment);
    }

    public void cancelAppointment(Long appointmentId) {


        Long currentCustomerId = 1L; // Replace with actual method to get current customer ID

        Appointment appointment = appointmentRepo.findByAppointmentIdAndCustomerId(appointmentId, currentCustomerId)
        .orElseThrow(() -> new DataNotFoundException("Appointment not found with id: " + appointmentId));


        if (appointment.getStatus().equals("Cancelled")) {
            throw new DataConflictException("Appointment is already cancelled");
        }

        if (appointment.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot cancel a completed appointment");
        }

        appointment.setStatus("Cancelled");
        appointmentRepo.save(appointment);
    }

}
