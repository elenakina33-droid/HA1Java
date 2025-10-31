package med.supply.system.model;

/**
 * Represents a single item stored or transferred by a storage vehicle.
 */
public class StorageItem {
    private final String sku;
    private final String name;
    private int quantity;

    public StorageItem(String sku, String name, int quantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        this.sku = sku.trim();
        this.name = name.trim();
        this.quantity = quantity;
    }

    // === Getters ===
    public String getSku() { return sku; }

    public String getName() { return name; }

    public int getQuantity() { return quantity; }

    // === Setter ===
    public void setQuantity(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity must be non-negative");
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StorageItem{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
