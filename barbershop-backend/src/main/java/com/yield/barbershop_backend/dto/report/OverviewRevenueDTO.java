package com.yield.barbershop_backend.dto.report;

import lombok.Data;

@Data

public class OverviewRevenueDTO {
    
    // Tổng số tiền thu được từ tất cả các giao dịch
    // Total revenue from all transactions
    private Double totalRevenue; 

    // Tổng danh thu trừ đi các khoản giảm giá, hoàn tiền.
    // Total revenue after discounts and refunds
    private Double netRevenue;

    // Tổng số giao dịch
    // Total number of transactions
    private Long totalTransactions;

    // Giá trị trung bình của giao dịch = Tổng số tiền thu được từ tất cả các giao dịch / Tổng số giao dịch
    // Average transaction amount
    private Double averageTransactionAmount;

    // Biến động doanh thu = ((Doanh thu hiện tại - Doanh thu trước) / Doanh thu trước) * 100
    // Revenue growth
    private Double revenueGrowth;

}
