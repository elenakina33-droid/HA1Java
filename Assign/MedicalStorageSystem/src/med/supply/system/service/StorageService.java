package med.supply.system.service;

import med.supply.system.model.*;
import med.supply.system.repository.Repository;
import med.supply.system.util.LogManager;
import med.supply.system.util.RegexUtils;

import java.io.IOException;

/**
 * Handles all storage operations — vehicles, stations, and items.
 * Now uses getters/setters from the model classes for safe encapsulation.
 */
public class StorageService {
    private final Repository repo;
    private final LogManager logs;

    public StorageService(Repository repo, LogManager logs) {
        this.repo = repo;
        this.logs = logs;
    }

    // -----------------------------------------------------------
    // Vehicle management
    // -----------------------------------------------------------
    public void addVehicle(StorageVehicle v) throws IOException {
        requireValidName(v.getName(), "vehicle");
        repo.vehicles.put(v.getId(), v);
        logs.logSystem("Vehicle added: " + v);
        logs.logVehicle(v.getName(), "created");
    }

    // -----------------------------------------------------------
    // Charging Station management
    // -----------------------------------------------------------
    public void addChargingStation(ChargingStation s) throws IOException {
        requireValidName(s.getName(), "station");
        repo.stations.put(s.getId(), s);
        logs.logSystem("Charging station added: " + s);
        logs.logCharging(s.getName(), "created");
    }

    public void updateChargingLoad(String stationId, int pct) throws IOException {
        ChargingStation s = repo.stations.get(stationId);
        if (s == null) throw new IllegalArgumentException("Charging station not found: " + stationId);
        s.setCurrentLoadPct(Math.max(0, Math.min(100, pct)));
        logs.logCharging(s.getName(), "Load set to " + s.getCurrentLoadPct() + "%");
        logs.logSystem("Charging load updated for " + s.getName());
    }

    // -----------------------------------------------------------
    // Inventory management
    // -----------------------------------------------------------
    public void addItemToVehicle(String vehicleId, StorageItem item) throws IOException {
        StorageVehicle v = repo.vehicles.get(vehicleId);
        if (v == null) throw new IllegalArgumentException("Vehicle not found: " + vehicleId);
        v.addItem(item);
        logs.logVehicle(v.getName(), "Added item " + item.getSku() + " x" + item.getQuantity());
        logs.logSystem("Inventory updated for " + v.getName() + " SKU=" + item.getSku());
    }

    // -----------------------------------------------------------
    // Validation helper
    // -----------------------------------------------------------
    private void requireValidName(String name, String kind) {
        if (!RegexUtils.isValidEquipment(name)) {
            throw new IllegalArgumentException(
                    "Invalid " + kind + " name (Must be up to 2–40 chars)."
            );
        }
    }
}
