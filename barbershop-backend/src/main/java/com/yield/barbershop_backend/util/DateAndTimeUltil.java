package com.yield.barbershop_backend.util;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Date;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

@Component
public class DateAndTimeUltil {
    
    public int getWeekOfYear(Date date) {
        WeekFields weekFields = WeekFields.of(java.time.DayOfWeek.SUNDAY, 1);
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().get(weekFields.weekOfWeekBasedYear());
    }

    public Date[] getWeekStartEnd(Date date) {

        
        LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        WeekFields wf = WeekFields.of(java.time.DayOfWeek.MONDAY, 1);
        LocalDate start = localDate.with(wf.dayOfWeek(), 1);
        LocalDate end = localDate.with(wf.dayOfWeek(), 7);

        Date startDate = Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atTime(23, 59, 59, 999_000_000).atZone(java.time.ZoneId.systemDefault()).toInstant());

        return new Date[] { startDate, endDate };
    }

    public Date[] getMonthStartEnd(Date date) {
        LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        LocalDate start = localDate.withDayOfMonth(1);
        LocalDate end = localDate.withDayOfMonth(localDate.lengthOfMonth());

        Date startDate = Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atTime(23, 59, 59, 999_000_000).atZone(java.time.ZoneId.systemDefault()).toInstant());

        return new Date[] { startDate, endDate };
    }

    public Date[] getYearStartEnd(Date date) {

        LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        LocalDate start = localDate.withDayOfYear(1);
        LocalDate end = localDate.withDayOfYear(localDate.lengthOfYear());

        Date startDate = Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atTime(23, 59, 59, 999_000_000).atZone(java.time.ZoneId.systemDefault()).toInstant());

        return new Date[] { startDate, endDate };

    }
    
}
