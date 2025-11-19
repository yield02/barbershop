package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.report.BarberRevenueDTO;
import com.yield.barbershop_backend.dto.report.CategoryRevenueDTO;
import com.yield.barbershop_backend.dto.report.OverviewRevenueDTO;
import com.yield.barbershop_backend.dto.report.ReportRevenueDTO;
import com.yield.barbershop_backend.service.ReportService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/revenue-summary")
    public ResponseEntity<ApiResponse<OverviewRevenueDTO>> getOverViewRevenue(@RequestParam String type, @RequestParam String date) throws ParseException {
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        if(type.equals("date")) {
            OverviewRevenueDTO overviewRevenue = reportService.getDateOverviewRevenue(dateFormat.parse(date));
            return ResponseEntity.ok(new ApiResponse<>(true, "", overviewRevenue));
        }
        else if(type.equals("week")) {
            OverviewRevenueDTO overviewRevenue = reportService.getWeekOverviewRevenue(dateFormat.parse(date));
            return ResponseEntity.ok(new ApiResponse<>(true, "", overviewRevenue));

        }
        else if(type.equals("month")) {
            OverviewRevenueDTO overviewRevenue = reportService.getMonthOverviewRevenue(dateFormat.parse(date));
            return ResponseEntity.ok(new ApiResponse<>(true, "", overviewRevenue));
        }
        else if(type.equals("year")) {
            OverviewRevenueDTO overviewRevenue = reportService.getYearOverviewRevenue(dateFormat.parse(date));
            return ResponseEntity.ok(new ApiResponse<>(true, "", overviewRevenue));
        }

        return null;
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<ApiResponse<List<CategoryRevenueDTO>>> getRevenueByCategory(@RequestParam String startDate, @RequestParam String endDate) throws BadRequestException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDateObj = null, endDateObj = null;
        try {
            startDateObj = dateFormat.parse(startDate);
            endDateObj = dateFormat.parse(endDate);
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format");
        }

        List<CategoryRevenueDTO> reportRevenueDTOS = reportService.getRevenueByCategory(startDateObj, endDateObj);
        return ResponseEntity.ok(new ApiResponse<>(true, "", reportRevenueDTOS));
    }

    @GetMapping("/revenue-by-barber")
    public ResponseEntity<ApiResponse<List<BarberRevenueDTO>>> getRevenueByBarber(@RequestParam String startDate, @RequestParam String endDate) throws BadRequestException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDateObj = null, endDateObj = null;
        try {
            startDateObj = dateFormat.parse(startDate);
            endDateObj = dateFormat.parse(endDate);
        } catch (ParseException e) {
            throw new BadRequestException("Invalid date format");
        }

        List<BarberRevenueDTO> reportRevenueDTOS = reportService.getRevenueByBarber(startDateObj, endDateObj);
        return ResponseEntity.ok(new ApiResponse<>(true, "", reportRevenueDTOS));
    }
    
    
    

}
