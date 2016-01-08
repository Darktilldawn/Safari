/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 socraticphoenix@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Socratic_Phoenix (socraticphoenix@gmail.com)
 */
package io.github.darktilldawn.safari.scheduler.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class SpongeRunnable implements Runnable {
    public static final UUID NOT_STARTED = UUID.randomUUID();

    private UUID id;
    private Scheduler service;

    public SpongeRunnable() {
        this.service = Sponge.getGame().getScheduler();
        this.id = SpongeRunnable.NOT_STARTED;
    }

    public synchronized void cancel() {
        Optional<Task> taskOptional = this.getTask();
        if(taskOptional.isPresent()) {
            taskOptional.get().cancel();
        }
    }

    public synchronized Task runTask(Object plugin) {
        this.testRunning();
        Task task = this.getTaskBuilder().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskLater(Object plugin, long delay, TimeUnit delayType) {
        this.testRunning();
        Task task = this.getTaskBuilder().delay(delay, delayType).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskLater(Object plugin, long delayTicks) {
        this.testRunning();
        Task task = this.getTaskBuilder().delayTicks(delayTicks).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimer(Object plugin, long ticksPeriod) {
        this.testRunning();
        Task task = this.getTaskBuilder().intervalTicks(ticksPeriod).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimer(Object plugin, long period, TimeUnit periodType) {
        Task task = this.getTaskBuilder().interval(period, periodType).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimerLater(Object plugin, long ticksPeriod, long ticksDelay) {
        this.testRunning();
        Task task = this.getTaskBuilder().intervalTicks(ticksPeriod).delayTicks(ticksDelay).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimerLater(Object plugin, long period, long delay, TimeUnit timeUnit) {
        Task task = this.getTaskBuilder().interval(period, timeUnit).delay(delay, timeUnit).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }


    public synchronized Task runTaskTimerAsynchronously(Object plugin, long ticksPeriod) {
        this.testRunning();
        Task task = this.getTaskBuilder().intervalTicks(ticksPeriod).async().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimerAsynchronously(Object plugin, long period, TimeUnit periodType) {
        Task task = this.getTaskBuilder().interval(period, periodType).async().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimerLaterAsynchronously(Object plugin, long ticksPeriod, long ticksDelay) {
        this.testRunning();
        Task task = this.getTaskBuilder().intervalTicks(ticksPeriod).delayTicks(ticksDelay).async().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskTimerLaterAsynchronously(Object plugin, long period, long delay, TimeUnit timeUnit) {
        Task task = this.getTaskBuilder().interval(period, timeUnit).delay(delay, timeUnit).async().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskAsynchronously(Object plugin) {
        this.testRunning();
        Task task = this.getTaskBuilder().async().submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskLaterAsynchronously(Object plugin, long delay, TimeUnit delayType) {
        this.testRunning();
        Task task = this.getTaskBuilder().async().delay(delay, delayType).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized Task runTaskAsynchronously(Object plugin, long delayTicks) {
        this.testRunning();
        Task task = this.getTaskBuilder().async().delayTicks(delayTicks).submit(plugin);
        this.id = task.getUniqueId();
        return task;
    }

    public synchronized UUID getId() {
        return this.id;
    }

    public synchronized boolean isRunning() {
        return this.id != SpongeRunnable.NOT_STARTED;
    }

    public synchronized Optional<Task> getTask() {
        return this.service.getTaskById(this.id);
    }

    private void testRunning() {
        if(this.isRunning()) {
            throw new IllegalArgumentException("Task '".concat(this.id.toString()).concat("' is already running!"));
        }
    }

    private Task.Builder getTaskBuilder() {
        return this.service.createTaskBuilder().execute(this);
    }

}
