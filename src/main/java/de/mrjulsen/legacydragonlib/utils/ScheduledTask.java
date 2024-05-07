package de.mrjulsen.legacydragonlib.utils;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

import net.minecraft.world.level.Level;

public final class ScheduledTask<T> {

    public static final int INFINITE_RUNTIME = -1;

    private final T data;
    private final TriFunction<T, Level, Integer, Boolean> action;
    private final int tickDelay;
    private final int maxIterations;
    private final Level level;

    // internal
    private int iteration = 0;
    private long currentTick = -1;
    private UUID id;
    
    private ScheduledTask(T data, Level level, int delay, int maxIterations, TriFunction<T, Level, Integer, Boolean> action) {
        this.data = data;
        this.tickDelay = delay;
        this.maxIterations = maxIterations;
        this.action = action;
        this.level = level;
    }

    /**
     * Creates a scheduled task.
     * @param <T>
     * @param data Additional custom data which can be used in each iteration.
     * @param delay Delay in game ticks between each iteration.
     * @param maxIterations Max iterations allowed. Can be cancelled at any time when returning {@code false}
     * @param action The action to run. Return {@code false} to cancel the task.
     * @return New {@code SchedulesTask} object.
     */
    public static <T> ScheduledTask<T> create(T data, Level level, int delay, int maxIterations, TriFunction<T, Level, Integer, Boolean> action) {
        ScheduledTask<T> task = new ScheduledTask<T>(data, level, delay, maxIterations, action);
        task.id = ScheduledTaskHolder.store(task);
        return task;
    }

    /**
     * Creates a scheduled task.
     * @param <T>
     * @param data Additional custom data which can be used in each iteration.
     * @param delay Delay in game ticks between each iteration.
     * @param action The action to run. Return {@code false} to cancel the task.
     * @return New {@code ScheduledTask} object.
     */
    public static <T> ScheduledTask<T> create(T data, Level level, int delay, TriFunction<T, Level, Integer, Boolean> action) {
        ScheduledTask<T> task = new ScheduledTask<T>(data, level, delay, INFINITE_RUNTIME, action);
        task.id = ScheduledTaskHolder.store(task);
        return task;
    }

    private void run() {
        currentTick++;
        if (currentTick % tickDelay != 0) {
            return;
        }

        if (!action.apply(data, level, iteration)) {
            ScheduledTaskHolder.delete(id);
        }

        iteration++;

        if (maxIterations >= 0 && iteration >= maxIterations) {
            ScheduledTaskHolder.delete(id);
        }
    }

    public UUID getId() {
        return id;
    }

    public void cancel() {
        ScheduledTaskHolder.delete(id);
    }

    public static int getRunningTasksCount() {
        return ScheduledTaskHolder.scheduledTasks.size();
    }

    public static void runScheduledTasks() {
        final Collection<ScheduledTask<?>> taskList = new ArrayList<>(ScheduledTaskHolder.scheduledTasks.values());
        taskList.forEach(x -> x.run());
    }

    public static void cancelAllTasks() {
        ScheduledTaskHolder.scheduledTasks.clear();
    }


    private static final class ScheduledTaskHolder {
        
        static final Map<UUID, ScheduledTask<?>> scheduledTasks = new HashMap<>();

        static UUID store(ScheduledTask<?> task) {
            UUID id = UUID.randomUUID();
            while (scheduledTasks.containsKey(id)) {
                id = UUID.randomUUID();
            }
            scheduledTasks.put(id, task);
            return id;
        }

        static void delete(UUID id) {
            if (scheduledTasks.containsKey(id)) {
                scheduledTasks.remove(id);
            }
        }
    }

}

