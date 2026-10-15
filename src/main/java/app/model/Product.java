package app.model;

public class Product {
    private int productId;
    private String barcode;
    private String name;
    private int categoryId;
    private double unitPrice;
    private int stockQuantity;

    public Product() {}

    public Product(int productId, String barcode, String name, int categoryId, double unitPrice, int stockQuantity) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.categoryId = categoryId;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    @Override
    public String toString() {
        return name + " (" + barcode + ") - $" + String.format("%.2f", unitPrice);
    }
}
