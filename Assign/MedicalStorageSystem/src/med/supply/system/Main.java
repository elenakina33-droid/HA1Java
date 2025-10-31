package med.supply.system;

import med.supply.system.exception.ExceptionHandler;
import med.supply.system.model.*;
import med.supply.system.repository.Repository;
import med.supply.system.service.*;
import med.supply.system.util.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        PathsConfig cfg = new PathsConfig();
        cfg.ensure();
        LogManager log = new LogManager(cfg);
        Repository repo = new Repository();
        StorageService storage = new StorageService(repo, log);
        TaskService tasks = new TaskService(repo, log);
        DataExchangeSimulator exchange = new DataExchangeSimulator(cfg, log);

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== Medical Supplies System ===");
                System.out.println("1) List vehicles & inventory");
                System.out.println("2) List charging stations");
                System.out.println("3) Add vehicle");
                System.out.println("4) Add charging station");
                System.out.println("5) Add item to vehicle");
                System.out.println("6) Update charging load");
                System.out.println("7) Create task");
                System.out.println("8) Update task status");
                System.out.println("9) Simulate data exchange (byte & char)");
                System.out.println("10) Open logs by equipment name or date (YYYY-MM-DD or 'system')");
                System.out.println("11) Archive all logs to ZIP");
                System.out.println("12) Move a log file");
                System.out.println("13) Delete a log file");
                System.out.println("14) List all tasks and statuses");
                System.out.println("15) Assign vehicle to charging station");
                System.out.println("0) Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> listVehicles(repo);
                        case "2" -> listStations(repo);
                        case "3" -> addVehicleUI(sc, storage);
                        case "4" -> addChargingUI(sc, storage);
                        case "5" -> addItemUI(sc, storage);
                        case "6" -> updateLoadUI(sc, storage);
                        case "7" -> createTaskUI(sc, tasks);
                        case "8" -> updateTaskUI(sc, tasks);
                        case "9" -> {
                            System.out.print("Enter Vehicle ID for data exchange: ");
                            String vid = sc.nextLine().trim();
                            StorageVehicle veh = repo.vehicles.get(vid);

                            if (veh == null) {
                                ExceptionHandler.handleVehicleNotFound(vid);
                            } else if (veh.getInventory().isEmpty()) {
                                System.out.println("Vehicle has no items! Add item first.");
                            } else {
                                exchange.simulate(veh);
                                System.out.println("Data exchange simulated for vehicle " + vid);
                                System.out.println("Files created in: " + cfg.exchangeRoot);

                                // Resource management example
                                try {
                                    Path file = cfg.exchangeRoot.resolve("exchange_" + veh.getId() + ".txt");
                                    String firstLine = exchange.previewFirstLineWithHandler(file);
                                    System.out.println("[RESOURCE] Preview: " + firstLine);
                                } catch (Exception e) {
                                    System.err.println("[RESOURCE ERROR] " + e.getMessage());
                                }
                            }
                        }

                        case "10" -> openLogsUI(sc, log);
                        case "11" -> {
                            Path zip = cfg.archiveRoot.resolve("logs-" +
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip");
                            MetadataManager.archiveZip(cfg.logsRoot, zip, cfg.metaIndex);
                            System.out.println("Archived to: " + zip);
                        }
                        case "12" -> moveFileUI(sc, cfg); // Multiple exceptions
                        case "13" -> deleteFileUI(sc, cfg);
                        case "14" -> listTasks(repo);
                        case "15" -> assignVehicleToStationUI(sc, repo);
                        case "0" -> {
                            System.out.println("Bye.");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (Exception ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                }
            }
        }
    }

    private static void listVehicles(Repository repo) {
        if (repo.vehicles.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }
        System.out.println("Vehicles:");
        for (StorageVehicle v : repo.vehicles.values()) {
            System.out.println(" - " + v);

            if (!v.getInventory().isEmpty()) {
                for (StorageItem it : v.getInventory().values()) {
                    System.out.println("    * " + it);
                }
            }
        }
    }


    private static void listStations(Repository repo) {
        if (repo.stations.isEmpty()) {
            System.out.println("No charging stations found.");
            return;
        }
        System.out.println("Charging Stations:");
        for (ChargingStation s : repo.stations.values()) {
            System.out.println(" - " + s);
        }
    }

    private static void addVehicleUI(Scanner sc, StorageService storage) throws IOException {
        System.out.print("Vehicle ID (e.g., VEH-001): ");
        String id = sc.nextLine().trim();
        System.out.print("Vehicle Name (alnum/_/-): ");
        String name = sc.nextLine().trim();
        storage.addVehicle(new StorageVehicle(id, name));
        System.out.println("Vehicle added.");
    }

    private static void addChargingUI(Scanner sc, StorageService storage) throws IOException {
        System.out.print("Station ID (e.g., CHG-001): ");
        String id = sc.nextLine().trim();
        System.out.print("Station Name (alnum/_/-): ");
        String name = sc.nextLine().trim();
        storage.addChargingStation(new ChargingStation(id, name));
        System.out.println("Charging station added.");
    }

    private static void addItemUI(Scanner sc, StorageService storage) throws IOException {
        System.out.print("Vehicle ID: ");
        String vid = sc.nextLine().trim();
        System.out.print("SKU: ");
        String sku = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(sc.nextLine().trim());
        storage.addItemToVehicle(vid, new StorageItem(sku, name, qty));
        System.out.println("Item added.");
    }

    private static void updateLoadUI(Scanner sc, StorageService storage) throws IOException {
        System.out.print("Station ID: ");
        String sid = sc.nextLine().trim();
        System.out.print("Load %: ");
        int pct = Integer.parseInt(sc.nextLine().trim());
        storage.updateChargingLoad(sid, pct);
        System.out.println("Load updated.");
    }

    private static void createTaskUI(Scanner sc, TaskService tasks) throws IOException {
        System.out.print("Task ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Description: ");
        String desc = sc.nextLine().trim();
        System.out.print("Assignee Vehicle ID (or empty): ");
        String assignee = sc.nextLine().trim();
        if (assignee.isBlank()) assignee = null;
        tasks.createTask(new Task(id, desc, assignee));
        System.out.println("Task created.");


    }

    private static void updateTaskUI(Scanner sc, TaskService tasks) throws Exception {
        System.out.print("Task ID: ");
        String id = sc.nextLine().trim();

        System.out.print("New status (PENDING, IN_PROGRESS, DONE): ");
        String s = sc.nextLine().trim().toUpperCase(Locale.ROOT);
        try {
            tasks.updateStatus(id, TaskStatus.valueOf(s));
        } catch (IllegalArgumentException e) {
            // Call the ExceptionHandler to re-throw with context
            med.supply.system.exception.ExceptionHandler.handleInvalidTaskStatus(s, e);
        }
        System.out.println("Task updated.");

        // 🔹 Ask the user to provide an energy reading (valid or invalid)
        System.out.print("Enter energy value (e.g., energy=100): ");
        String energyInput = sc.nextLine().trim();

        // 🔹 Dynamically parse user input — may throw re-thrown exception

    }


    private static void openLogsUI(Scanner sc, LogManager logs) throws Exception {
        System.out.print("Enter equipment name (e.g., Van_Alpha), 'system', or date YYYY-MM-DD: ");
        String key = sc.nextLine().trim();

        try {
            var found = logs.findByEquipmentOrDate(key);
            if (found.isEmpty()) {
                // Simulate resource error (missing log files)
                throw new java.io.FileNotFoundException("No logs found for input: " + key);
            }

            System.out.println("Found logs:");
            for (int i = 0; i < found.size(); i++) {
                System.out.println("  [" + i + "] " + found.get(i));
            }

            System.out.print("Open which index? ");

            try {
                int idx = Integer.parseInt(sc.nextLine().trim());
                if (idx < 0 || idx >= found.size()) {
                    // Throw via ExceptionHandler (invalid index)
                    med.supply.system.exception.ExceptionHandler.handleInvalidLogIndex(
                            idx, found.size(),
                            new IndexOutOfBoundsException("Index " + idx + " out of range"));
                }

                var p = found.get(idx);
                System.out.println("--- " + p + " ---");

                // Use the ExceptionHandler’s resource-managed read
                try {
                    String content = med.supply.system.exception.ExceptionHandler.readFirstLine(p);
                    System.out.println("[RESOURCE] First line: " + content);
                    System.out.println("Full log content:\n" + logs.readLog(p));
                } catch (Exception e) {
                    System.err.println("[RESOURCE ERROR] " + e.getMessage());
                    e.printStackTrace(System.err);
                }

                System.out.println("---------------");

            } catch (NumberFormatException e) {
                // User typed non-numeric input for index
                med.supply.system.exception.ExceptionHandler.handleInvalidLogIndex(
                        -1, found.size(), e);
            }

        } catch (java.io.FileNotFoundException e) {
            // Wrap and re-throw via ExceptionHandler
            med.supply.system.exception.ExceptionHandler.handleMissingLog(key, e);
        }
    }


    private static void moveFileUI(Scanner sc, PathsConfig cfg) {
        try {
            LogManager logManager = new LogManager(cfg);
            System.out.print("From (path): ");
            Path from = Path.of(sc.nextLine().trim());
            System.out.print("To (path): ");
            Path to = Path.of(sc.nextLine().trim());

            // Multiple exception example
            logManager.archiveLogWithHandler(from, to);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void deleteFileUI(Scanner sc, PathsConfig cfg) throws IOException {
        System.out.print("File to delete (path): ");
        Path file = Path.of(sc.nextLine().trim());
        MetadataManager.delete(file, cfg.metaIndex);
        System.out.println("Deleted.");
    }

    private static void listTasks(Repository repo) {
        if (repo.tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        System.out.println("Tasks:");
        for (Task t : repo.tasks.values()) {
            System.out.printf(" - [%s] %s | Vehicle: %s | Status: %s%n",
                    t.id, t.description,
                    (t.assigneeVehicleId == null ? "Unassigned" : t.assigneeVehicleId),
                    t.status);
        }
    }

    private static void assignVehicleToStationUI(Scanner sc, Repository repo) {
        System.out.print("Vehicle ID: ");
        String vid = sc.nextLine().trim();
        var vehicle = repo.vehicles.get(vid);
        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return;
        }

        System.out.print("Station ID: ");
        String sid = sc.nextLine().trim();
        var station = repo.stations.get(sid);
        if (station == null) {
            System.out.println("Charging station not found.");
            return;
        }

        vehicle.setAssignedStationId(sid);
        System.out.println("Vehicle " + vehicle.getId() + " assigned to station " + sid + ".");
    }
}
