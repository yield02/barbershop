package com.yield.barbershop_backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
public class AppointmentService {

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

    @Autowired
    private PromotionItemService promotionItemService;

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
    public Appointment createAppointment(CreateAppointmentDTO appointment)
            throws DataNotFoundException, DataConflictException {
        // Check and get user
        User user = userService.getUserById(appointment.getUserId());

        // Check services
        List<com.yield.barbershop_backend.model.Service> dbServices = serviceService
                .findExistedServiceIds(appointment.getServiceIds());

        Map<Long, com.yield.barbershop_backend.model.Service> dbServicesMap = dbServices.stream()
                .collect(Collectors.toMap(com.yield.barbershop_backend.model.Service::getServiceId, item -> item));

        if (!dbServices.isEmpty() && (dbServices.size() != appointment.getServiceIds().stream().collect(Collectors.toSet()).size())) {

            List<Long> nonExistedIds = appointment.getServiceIds().stream()
                    .filter(id -> !dbServices.stream().map(com.yield.barbershop_backend.model.Service::getServiceId)
                            .collect(Collectors.toSet()).contains(id))
                    .toList();

            throw new DataNotFoundException("Services not found with ids: " + nonExistedIds);
        }

        // Caculate endTime
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(
                dbServices.stream()
                        .map(com.yield.barbershop_backend.model.Service::getDurationMinutes)
                        .reduce(0, Integer::sum));

        // <ServiceId, PromotionItem>

        Set<PromotionItem> listPromotionItems = dbServices.stream().flatMap(service -> {
            return service.getPromotionItems().stream();
        }).collect(Collectors.toSet());

        // serviceId, PromotionItemList

        Map<Long, List<PromotionItem>> promotionItemsGroupByServiceId = listPromotionItems.stream()
                .collect(Collectors.groupingBy(PromotionItem::getServiceId));

        Set<Long> dbPromotionIds = listPromotionItems.stream().map(PromotionItem::getPromotionId)
                .collect(Collectors.toSet());

        Map<Long, Promotion> activePromotions = promotionService
                .getActivePromotionsByIds(dbPromotionIds.stream().toList()).stream()
                .collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));
        // <promotionId, Promotion>
        Map<Long, Promotion> promotionsNeedingMaxApplicableQuantityUpdateMap = new HashMap<>();

        // create new appointmentservices
        List<com.yield.barbershop_backend.model.AppointmentService> newAppointmentServices = appointment.getServiceIds().stream()
                .map(serviceId -> {

                    com.yield.barbershop_backend.model.Service service = dbServicesMap.get(serviceId);

                    List<PromotionItem> promotionItems = promotionItemsGroupByServiceId.get(service.getServiceId());

                    PromotionItem bestPromotionItem = null;

                    if (promotionItems != null) {
                        bestPromotionItem = promotionItems.size() == 1 ? promotionItems.get(0)
                                : promotionItems.stream().max((PromotionItem p1, PromotionItem p2) -> {
                                    Promotion promotion1 = activePromotions.get(p1.getPromotionId());
                                    Promotion promotion2 = activePromotions.get(p2.getPromotionId());
                                    if (promotion1.getDiscountAmount() != null &&
                                            promotion2.getDiscountAmount() != null &&
                                            promotion1.getDiscountAmount() >= promotion2.getDiscountAmount())
                                        return 1;
                                    else if (promotion1.getDiscountPercentage() != null &&
                                            promotion2.getDiscountPercentage() != null &&
                                            promotion1.getDiscountPercentage() >= promotion2.getDiscountPercentage())
                                        return 1;
                                    return -1;
                                }).orElse(null);
                    } 

                    Double originalPrice = service.getPrice();
                    Double discountAmount = 0.0;
                    Double finalPrice = originalPrice;

                    com.yield.barbershop_backend.model.AppointmentService newAppointmentService = new com.yield.barbershop_backend.model.AppointmentService();

                    if (bestPromotionItem != null) {

                        Promotion promotion = activePromotions.get(bestPromotionItem.getPromotionId());

                        if (promotion != null && promotion.getMaxApplicableQuantity() > 0) {
                            // Minus MaxApplicableQuantity
                            promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() - 1);
                            if (!promotionsNeedingMaxApplicableQuantityUpdateMap.containsKey(promotion.getPromotionId())) {
                                promotionsNeedingMaxApplicableQuantityUpdateMap.put(promotion.getPromotionId(), promotion);
                            }
                            else {
                                promotionsNeedingMaxApplicableQuantityUpdateMap.replace(promotion.getPromotionId(), promotion);
                            } 

                            if (promotion.getDiscountAmount() != null) {
                                discountAmount = promotion.getDiscountAmount();
                            }

                            if (promotion.getDiscountPercentage() != null) {
                                discountAmount = service.getPrice() * promotion.getDiscountPercentage() / 100;
                            }

                            finalPrice = originalPrice - discountAmount;
                            newAppointmentService.setPromotionId(promotion.getPromotionId());
                        }
                    }

                    newAppointmentService.setServiceId(service.getServiceId());
                    newAppointmentService.setServiceName(service.getServiceName());
                    newAppointmentService.setOriginalPrice(originalPrice);
                    newAppointmentService.setDiscountAmount(discountAmount);
                    newAppointmentService.setFinalPrice(finalPrice);
                    return newAppointmentService;
                }).collect(Collectors.toList());

        // Minus MaxApplicableQuantity
        if (promotionsNeedingMaxApplicableQuantityUpdateMap.size() > 0)
            promotionService.updatePromotions(promotionsNeedingMaxApplicableQuantityUpdateMap.values().stream().toList());

        // Caculate totalAmounts
        Double totalFinalPrice = newAppointmentServices.stream()
                .mapToDouble(com.yield.barbershop_backend.model.AppointmentService::getFinalPrice).sum();

        Customer customer = customerService.getCustomerById(appointment.getCustomerId());

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

        // Set AppointmentId for AppointmentServices
        for (com.yield.barbershop_backend.model.AppointmentService appointmentService : newAppointmentServices) {
            appointmentService.setAppointmentId(savedAppointment.getAppointmentId());
        }

        // Save AppointmentServices
        appointmentServiceService.createAppointmentServices(newAppointmentServices);

        return savedAppointment;
    }

    @Transactional
    public Appointment updateAppointment(Long appointmentId, UpdateAppointmentDTO appointment)
            throws AccessDeniedException {

        // Get existing appointment
        Appointment existingAppointment = getAppointmentById(appointmentId);

        // Checking status
        if (existingAppointment.getStatus().equals("Cancelled")
                || existingAppointment.getStatus().equals("Completed")) {
            throw new DataConflictException(
                    "Cannot update appointment with status: " + existingAppointment.getStatus());
        }

        // Check owner and admin
        if (appointment.getCustomerId() != null
                && !existingAppointment.getCustomerId().equals(appointment.getCustomerId())) {
            throw new AccessDeniedException("You don't have permission to update this appointment");
        }

        // Get existing appointmentServices
        List<com.yield.barbershop_backend.model.AppointmentService> existingAppointmentServices = existingAppointment
                .getAppointmentServices();
        List<Long> existingServiceIds = existingAppointmentServices.stream()
                .map(com.yield.barbershop_backend.model.AppointmentService::getServiceId).collect(Collectors.toList());

        Boolean isDifference = false;

        if (existingServiceIds.size() != appointment.getServiceIds().size()) {
            isDifference = true;
        } else if (!existingServiceIds.equals(appointment.getServiceIds())) {
            isDifference = true;
        }

        if (isDifference) {
            // Check new services are existing
            Set<Long> newServiceIds = new HashSet<>(appointment.getServiceIds());

            List<com.yield.barbershop_backend.model.Service> dbNewServices = serviceService
                    .getActiveServicesByIds(newServiceIds);
            Map<Long, com.yield.barbershop_backend.model.Service> dbServiceMap = dbNewServices.stream()
                    .collect(Collectors.toMap(com.yield.barbershop_backend.model.Service::getServiceId,
                            service -> service));

            if (newServiceIds.size() != dbNewServices.size()) {
                Set<Long> dbServiceIds = dbNewServices.stream()
                        .map(com.yield.barbershop_backend.model.Service::getServiceId).collect(Collectors.toSet());
                throw new DataNotFoundException(
                        "Service not found: " + newServiceIds.removeIf(id -> dbServiceIds.contains(id)));
            }

            // Delete old appointmentServices
            appointmentServiceService.deleteAppointmentServicesByAppointmentId(appointmentId);

            // return max applicable quantity
            // <PromotionId, Promotion>
            Map<Long, Promotion> returnMaxApplicableQuantityPromotionsMap = promotionService
                    .getActivePromotionsByIds(existingAppointmentServices.stream()
                            .map(com.yield.barbershop_backend.model.AppointmentService::getPromotionId).toList(), existingAppointment.getCreatedAt())
                    .stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

            if (returnMaxApplicableQuantityPromotionsMap.size() > 0) {
                existingAppointmentServices.forEach(appointmentService -> {

                    if (appointmentService.getPromotionId() == null) {
                        return;
                    }

                    Promotion promotion = returnMaxApplicableQuantityPromotionsMap
                            .get(appointmentService.getPromotionId());
                    if (promotion != null) {
                        promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() + 1);
                        returnMaxApplicableQuantityPromotionsMap.replace(promotion.getPromotionId(), promotion);
                    }
                });
            }
            promotionService.updatePromotions(returnMaxApplicableQuantityPromotionsMap.values().stream().toList());

            // Get promotion and calculate promotions
            List<PromotionItem> promotionItems = dbNewServices.stream()
                    .flatMap(service -> service.getPromotionItems().stream()).toList();
            Set<Long> promotionIds = promotionItems.stream()
                    .filter(promotionItem -> promotionItem.getPromotionId() != null)
                    .map(promotionItem -> promotionItem.getPromotionId())
                    .collect(Collectors.toSet());
            List<Promotion> dbActivePromotions = promotionService
                    .getActivePromotionsByIds(promotionIds.stream().toList());
            // <promotionId, promotion>
            Map<Long, Promotion> promotionMap = dbActivePromotions.stream()
                    .collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

            // <serviceId, promotion>
            Map<Long, Promotion> servicePromotionMap = new HashMap<>();

            promotionItems.forEach(promotionItem -> {
                Long promotionId = promotionItem.getPromotionId();
                if (promotionId != null) {
                    Long serviceId = promotionItem.getServiceId();
                    Promotion promotion = promotionMap.get(promotionId);
                    if (servicePromotionMap.containsKey(serviceId)) {
                        Promotion existingPromotion = servicePromotionMap.get(serviceId);
                        Promotion betterPromotion = promotionService.pickBetterPromotion(existingPromotion, promotion);
                        servicePromotionMap.replace(serviceId, betterPromotion);

                    } else {
                        servicePromotionMap.put(serviceId, promotion);
                    }
                }
            });

            List<com.yield.barbershop_backend.model.AppointmentService> newAppointmentServices = new ArrayList<>();

            for (Long serviceId : appointment.getServiceIds()) {
                com.yield.barbershop_backend.model.AppointmentService newAppointmentService = new com.yield.barbershop_backend.model.AppointmentService();
                Double originalPrice = dbServiceMap.get(serviceId).getPrice();
                Double discountAmount = 0.0;
                Double finalPrice = originalPrice;

                Promotion promotion = servicePromotionMap.get(serviceId);

                if (promotion != null && promotion.getMaxApplicableQuantity() > 0) {
                    discountAmount = promotion.getDiscountAmount() != null ? promotion.getDiscountAmount()
                            : promotion.getDiscountPercentage() * originalPrice / 100.0;
                    finalPrice = originalPrice - discountAmount;

                    newAppointmentService.setPromotionId(servicePromotionMap.get(serviceId).getPromotionId());

                    // Reduce maxApplicableQuantity
                    promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() - 1);
                    servicePromotionMap.replace(serviceId, promotion);
                }

                newAppointmentService.setAppointmentId(appointmentId);
                newAppointmentService.setServiceName(dbServiceMap.get(serviceId).getServiceName());
                newAppointmentService.setServiceId(serviceId);
                newAppointmentService.setOriginalPrice(originalPrice);
                newAppointmentService.setDiscountAmount(discountAmount);
                newAppointmentService.setFinalPrice(finalPrice);

                newAppointmentServices.add(newAppointmentService);
            }

            Double totalAmount = newAppointmentServices.stream()
                    .mapToDouble(com.yield.barbershop_backend.model.AppointmentService::getFinalPrice).sum();
            existingAppointment.setTotalAmount(totalAmount);

            // Create new appointment services
            List<com.yield.barbershop_backend.model.AppointmentService> createdAppointmentServices = appointmentServiceService
                    .createAppointmentServices(newAppointmentServices);
            existingAppointment.setAppointmentServices(createdAppointmentServices);

            // save promotion minus quantity
            List<Promotion> promotionsToUpdateMaxApplicableQuantity = servicePromotionMap.values().stream().toList();
            promotionService.updatePromotions(promotionsToUpdateMaxApplicableQuantity);

        } else {
            // Recalculate if start time is different
            if (!appointment.getStartTime().equals(existingAppointment.getStartTime())) {
                Set<Long> currentServiceIds = existingAppointmentServices.stream()
                        .map(appointmentService -> appointmentService.getServiceId()).collect(Collectors.toSet());
                List<com.yield.barbershop_backend.model.Service> currentServices = serviceService
                        .getActiveServicesByIds(currentServiceIds);
                Long totalMinutes = currentServices.stream()
                        .mapToLong(com.yield.barbershop_backend.model.Service::getDurationMinutes).sum();

                LocalDateTime newStartTime = appointment.getStartTime();
                LocalDateTime newEndTime = newStartTime.plusMinutes(totalMinutes);

                // Check for appointment conflicts
                checkAppointmentConflictExceptCurrent(newStartTime, newEndTime, appointment.getUserId(),
                        existingAppointment.getAppointmentId());
            }
        }

        existingAppointment.setNotes(appointment.getNotes());
        existingAppointment.setUserId(appointment.getUserId());
        existingAppointment.setUpdatedAt(appointment.getUpdatedAt());

        Appointment updatedAppointment = appointmentRepo.save(existingAppointment);

        return updatedAppointment;
    }

    public List<Appointment> checkAppointmentsConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        return appointmentRepo.findAll(AppointmentSpecification.checkAppointmentsConflict(startTime, endTime, userId));
    }

    public void checkAppointmentConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);
        if (!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId)
                    .collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

    public void checkAppointmentConflictExceptCurrent(LocalDateTime startTime, LocalDateTime endTime, Long userId,
            Long currentAppointmentId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);

        conflictingAppointments.removeIf(appointment -> appointment.getAppointmentId().equals(currentAppointmentId));

        if (!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId)
                    .collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

    public void updateStatusAppointment(Long appointmentId, UpdateStatusAppointmentDTO status) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus().equals("Cancelled") || appointment.getStatus().equals("Completed")) {
            throw new DataConflictException(
                    "Cannot update status of appointment with status: " + appointment.getStatus());
        }
        appointment.setStatus(status.getStatus());
        appointmentRepo.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long currentCustomerId) {

        Appointment appointment = appointmentRepo.findByAppointmentIdAndCustomerId(appointmentId, currentCustomerId)
                .orElseThrow(() -> new DataNotFoundException("Appointment not found with id: " + appointmentId));

        if(!appointment.getCustomerId().equals(currentCustomerId)) {
            throw new AccessDeniedException("You don't have permission to cancel this appointment");
        }

        if (appointment.getStatus().equals("Cancelled")) {
            throw new DataConflictException("Appointment is already cancelled");
        }

        if (appointment.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot cancel a completed appointment");
        }

        List<com.yield.barbershop_backend.model.AppointmentService> newAppointmentServices = appointment
                .getAppointmentServices();

        List<Long> promotionIds = newAppointmentServices.stream().filter(appointmentService -> {
            return appointmentService.getPromotionId() != null;
        }).map(com.yield.barbershop_backend.model.AppointmentService::getPromotionId).toList();

        // <promotionId, promotion>
        List<Promotion> promotions = promotionService.getActivePromotionsByIds(promotionIds, appointment.getCreatedAt());
        Map<Long, Promotion> promotionsMap = promotions.stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

        promotionIds.forEach(promotionId -> {
            Promotion promotion = promotionsMap.get(promotionId);
            if(promotion != null) {
                promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() + 1);
                promotionsMap.replace(promotionId, promotion);
            }
        });

        promotionService.updatePromotions(promotionsMap.values().stream().toList());

        appointment.setStatus("Cancelled");
        appointmentRepo.save(appointment);
    }

}
