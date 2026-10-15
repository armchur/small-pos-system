package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int saleId;
    private String receiptNumber;
    private LocalDateTime saleTimestamp;
    private double subtotal;
    private double taxAmount;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod; // CASH, CARD, TRANSFER
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {}

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public LocalDateTime getSaleTimestamp() { return saleTimestamp; }
    public void setSaleTimestamp(LocalDateTime saleTimestamp) { this.saleTimestamp = saleTimestamp; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public void addItem(SaleItem item) {
        this.items.add(item);
    }
}
