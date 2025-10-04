package com.yield.barbershop_backend.specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.model.Payment;
import com.yield.barbershop_backend.util.DateAndTimeUltil;

public class PaymentSpecification {
    

    public static Specification<Payment> getPaymentCurrentAndPreviousDates(Date date) {
        LocalDate dateLocal = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date startDate = Date.from(dateLocal.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(dateLocal.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant()); 
        return (root, query, cb) -> cb.between(root.get("paymentDate"), startDate, endDate);
    }

    public static Specification<Payment> getPaymentCurrentAndPreviousWeek(Date date) {

        DateAndTimeUltil dateAndTimeUltil = new DateAndTimeUltil();
        Date[] dates = dateAndTimeUltil.getWeekStartEnd(date);
        Date startDay = dates[0];
        Date endDay = dates[1];
        Date startDayLastWeek = Date.from(startDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return (root, query, cb) -> cb.between(root.get("paymentDate"), startDayLastWeek, endDay);

    }

    public static Specification<Payment> getPaymentCurrentAndPreviousMonth(Date date) {

        DateAndTimeUltil dateAndTimeUltil = new DateAndTimeUltil();
        Date[] dates = dateAndTimeUltil.getMonthStartEnd(date);
        Date startDay = dates[0];
        Date endDay = dates[1];
        Date startDayLastMonth = Date.from(startDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return (root, query, cb) -> cb.between(root.get("paymentDate"), startDayLastMonth, endDay);
    }

    public static Specification<Payment> getPaymentCurrentAndPreviousYear(Date date) {

        DateAndTimeUltil dateAndTimeUltil = new DateAndTimeUltil();
        Date[] dates = dateAndTimeUltil.getYearStartEnd(date);
        Date startDay = dates[0];
        Date endDay = dates[1];
        Date startDayLastYear = Date.from(startDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return (root, query, cb) -> cb.between(root.get("paymentDate"), startDayLastYear, endDay);
    }

}