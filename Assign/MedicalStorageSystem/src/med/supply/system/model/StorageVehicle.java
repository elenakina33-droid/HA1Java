package med.supply.system.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an automated storage vehicle that can store and transfer items.
 */
public class StorageVehicle {
    private final String id;
    private final String name;
    private int batteryLevelPct = 100;
    private String assignedStationId;
    private final Map<String, StorageItem> inventory = new HashMap<>();

    public StorageVehicle(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.id = id.trim();
        this.name = name.trim();
    }

    // === Getters ===
    public String getId() { return id; }

    public String getName() { return name; }

    public int getBatteryLevelPct() { return batteryLevelPct; }

    public String getAssignedStationId() { return assignedStationId; }

    public Map<String, StorageItem> getInventory() { return inventory; }

    // === Setters ===
    public void setBatteryLevelPct(int batteryLevelPct) {
        if (batteryLevelPct < 0 || batteryLevelPct > 100)
            throw new IllegalArgumentException("Battery level must be 0–100");
        this.batteryLevelPct = batteryLevelPct;
    }

    public void setAssignedStationId(String assignedStationId) {
        this.assignedStationId = assignedStationId;
    }

    // === Inventory operations ===
    public void addItem(StorageItem item) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        inventory.merge(item.getSku(), item, (a, b) -> {
            a.setQuantity(a.getQuantity() + b.getQuantity());
            return a;
        });
    }

    @Override
    public String toString() {
        return "StorageVehicle{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", items=" + inventory.size() +
                ", battery=" + batteryLevelPct + "%" +
                (assignedStationId != null ? ", station='" + assignedStationId + "'" : "") +
                '}';
    }
}
