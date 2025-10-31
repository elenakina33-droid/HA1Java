package med.supply.system.service;

import med.supply.system.exception.ExceptionHandler;
import med.supply.system.model.*;
import med.supply.system.repository.Repository;
import med.supply.system.util.LogManager;

import java.io.IOException;

/**
 * Handles creation, assignment, and status updates of system tasks.
 */
public class TaskService {
    private final Repository repo;
    private final LogManager logs;

    public TaskService(Repository repo, LogManager logs) {
        this.repo = repo;
        this.logs = logs;
    }

    // -----------------------------------------------------------
    // Task creation
    // -----------------------------------------------------------
    public void createTask(Task t) throws IOException {
        //  Validate Task ID
        if (t.id == null || t.id.isBlank()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }

        // Validate description
        if (t.description == null || t.description.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be empty");
        }

        // Validate vehicle if assigned
        if (t.assigneeVehicleId != null && !repo.vehicles.containsKey(t.assigneeVehicleId)) {
            throw new IllegalArgumentException("Assigned vehicle does not exist: " + t.assigneeVehicleId);
        }

        // Store task
        repo.tasks.put(t.id, t);
        logs.logSystem("Task created: " + t.id + " -> " + t.description);

        // Log vehicle assignment if exists
        if (t.assigneeVehicleId != null) {
            StorageVehicle v = repo.vehicles.get(t.assigneeVehicleId);
            if (v != null) {
                logs.logVehicle(v.getName(), "Assigned task " + t.id + ": " + t.description);
            }
        }
    }

    // -----------------------------------------------------------
    // Task status updates
    // -----------------------------------------------------------
    public void updateStatus(String taskId, TaskStatus status) throws Exception {
        Task t = repo.tasks.get(taskId);
        if (t == null) {
            try {
                throw new IllegalArgumentException("Task not found: " + taskId);
            } catch (IllegalArgumentException e) {
                ExceptionHandler.handleTaskNotFound(taskId, e);
            }
        }

        t.status = status;
        logs.logSystem("Task " + taskId + " status -> " + status);

        if (t.assigneeVehicleId != null) {
            StorageVehicle v = repo.vehicles.get(t.assigneeVehicleId);
            if (v != null) {
                logs.logVehicle(v.getName(), "Task " + t.id + " status -> " + status);
            }
        }
    }

    // -----------------------------------------------------------
    // Demonstration: chained exceptions
    // -----------------------------------------------------------
    public void scheduleWithChaining(String taskId, String vehicleId) {
        try {
            ExceptionHandler.scheduleTask(taskId, vehicleId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Scheduling failed in TaskService for task=" + taskId +
                            ", vehicle=" + vehicleId, e
            );
        }
    }
}
