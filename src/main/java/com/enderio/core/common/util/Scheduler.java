package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.Handlers.Handler.Inst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Convenience helper to schedule events to happen in the future.
 */
@Handler(getInstFrom = Inst.METHOD)
public class Scheduler {

    /**
     * A task to be executed later.
     *
     */
    public static interface ITask {

        /**
         * This method will be called at the next post tick event. It can return a new task (or itself) to be enqueued
         * again.
         *
         * @return null or a task to be enqueued
         */
        ITask run();
    }

    private static final class Task implements ITask {

        private int delay;
        private Runnable toRun;

        private Task(int delay, Runnable toRun) {
            this.delay = delay;
            this.toRun = toRun;
        }

        @Override
        public ITask run() {
            if (delay <= 0) {
                toRun.run();
                return null;
            } else {
                delay--;
                return this;
            }
        }
    }

    private final Queue<ITask> clientQueue;
    private final Queue<ITask> serverQueue = new ConcurrentLinkedQueue<ITask>();

    /**
     * Please use the single instance available from instance().
     */
    public Scheduler(boolean isServer) {
        if (isServer) {
            clientQueue = null;
        } else {
            clientQueue = new ConcurrentLinkedQueue<ITask>();
        }
    }

    /**
     * Schedules a task to be called later
     *
     * @param delay The amount of ticks to delay the call
     * @param task  The {@link Runnable} to be run when the delay is up
     *
     * @see #schedule(int, Runnable, Side)
     */
    public void schedule(int delay, Runnable task) {
        schedule(delay, task, Side.SERVER);
    }

    /**
     * Schedules a task to be called later
     *
     * @param delay The amount of ticks to delay the call
     * @param task  The {@link Runnable} to be run when the delay is up
     * @param side  The side to schedule the task on.
     *              <p>
     *              Note: passing in {@link Side#CLIENT} on a dedicated server will work, but your task will never be
     *              called. Please avoid doing this to save processing.
     */
    public void schedule(int delay, Runnable task, Side side) {
        schedule(new Task(delay, task), side);
    }

    /**
     * Schedules a task to be called later
     *
     * @param task The {@link ITask} to be run at the next tick
     * @param side The side to schedule the task on.
     *             <p>
     *             Note: passing in {@link Side#CLIENT} on a dedicated server will work, but your task will never be
     *             called. Please avoid doing this to save processing.
     */
    public void schedule(ITask task, Side side) {
        if (side == Side.SERVER) {
            serverQueue.add(task);
        } else if (clientQueue != null) {
            clientQueue.add(task);
        }
    }

    /**
     * Returns the {@link Scheduler} instance for the current side.
     *
     * @see #schedule(int, Runnable)
     *
     * @return The {@link Scheduler} instance.
     */
    public static Scheduler instance() {
        return EnderCore.proxy.getScheduler();
    }

    /**
     * For internal use only. Do not call.
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            runTasks(serverQueue);
        }
    }

    /**
     * For internal use only. Do not call.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            runTasks(clientQueue);
            if (!serverQueue.isEmpty() && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
                serverQueue.clear();
            }
        }
    }

    private void runTasks(Queue<ITask> queue) {
        if (!queue.isEmpty()) {
            List<ITask> newtasks = new ArrayList<ITask>(queue.size());
            while (!queue.isEmpty()) {
                ITask task = queue.poll();
                if (task != null) {
                    task = task.run();
                    if (task != null) {
                        newtasks.add(task);
                    }
                }
            }
            for (ITask task : newtasks) {
                queue.add(task);
            }
        }
    }

}
