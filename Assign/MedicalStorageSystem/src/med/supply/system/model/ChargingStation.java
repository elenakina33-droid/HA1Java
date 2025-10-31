package med.supply.system.model;

/**
 * Represents a charging station used by storage vehicles.
 * Includes validation, encapsulation, and safe access methods.
 */
public class ChargingStation {

    private final String id;
    private final String name;
    private int currentLoadPct; // 0–100

    /**
     * Creates a new charging station.
     *
     * @param id   unique station ID, must not be blank
     * @param name human-readable name, must not be blank
     * @throws IllegalArgumentException if id or name is null/blank
     */
    public ChargingStation(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.currentLoadPct = 0; // default to 0%
    }

    // === Getters ===
    public String getId() { return id; }

    public String getName() { return name; }

    public int getCurrentLoadPct() { return currentLoadPct; }

    // === Setters ===
    /**
     * Sets the current load percentage (0–100%).
     *
     * @param pct new load percentage
     * @throws IllegalArgumentException if pct is outside 0–100
     */
    public void setCurrentLoadPct(int pct) {
        if (pct < 0 || pct > 100) {
            throw new IllegalArgumentException("currentLoadPct must be between 0 and 100");
        }
        this.currentLoadPct = pct;
    }

    // === Utility Methods ===
    @Override
    public String toString() {
        return "ChargingStation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", load=" + currentLoadPct + "%}";
    }

    /**
     * Two stations are equal if they share the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChargingStation)) return false;
        ChargingStation that = (ChargingStation) o;
        return id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }
}
