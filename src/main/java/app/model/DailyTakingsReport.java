package app.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DailyTakingsReport {
    private final LocalDate date;
    private final int totalSalesCount;
    private final double totalRevenue;
    private final List<PaymentBreakdown> paymentBreakdowns;

    public DailyTakingsReport(LocalDate date, int totalSalesCount, double totalRevenue, List<PaymentBreakdown> paymentBreakdowns) {
        this.date = date;
        this.totalSalesCount = totalSalesCount;
        this.totalRevenue = totalRevenue;
        this.paymentBreakdowns = paymentBreakdowns != null ? paymentBreakdowns : Collections.emptyList();
    }

    public LocalDate getDate() {
        return date;
    }

    public int getTotalSalesCount() {
        return totalSalesCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public List<PaymentBreakdown> getPaymentBreakdowns() {
        return paymentBreakdowns;
    }

    public static class PaymentBreakdown {
        private final String paymentMethod;
        private final int transactionCount;
        private final double subtotal;

        public PaymentBreakdown(String paymentMethod, int transactionCount, double subtotal) {
            this.paymentMethod = paymentMethod;
            this.transactionCount = transactionCount;
            this.subtotal = subtotal;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public double getSubtotal() {
            return subtotal;
        }
    }
}
