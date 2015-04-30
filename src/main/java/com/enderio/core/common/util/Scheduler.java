package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.Handlers.Handler.HandlerType;
import com.enderio.core.common.Handlers.Handler.Inst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Convenience helper to schedule events to happen in the future.
 */
@Handler(value = HandlerType.FML, getInstFrom = Inst.METHOD)
public class Scheduler
{
    @AllArgsConstructor
    private static final class Task
    {
        private int delay;
        private Runnable toRun;
        private Side side;

        private boolean run()
        {
            if (delay <= 0)
            {
                toRun.run();
                return true;
            }
            delay--;
            return false;
        }
    }

    private final List<Task> tasks = new ArrayList<Task>();

    /**
     * Schedules a task to be called later
     * 
     * @param delay
     *            The amount of ticks to delay the call
     * @param task
     *            The {@link Runnable} to be run when the delay is up
     * 
     * @see {@link #schedule(int, Runnable, Side)} for more details.
     */
    public void schedule(int delay, Runnable task)
    {
        schedule(delay, task, Side.SERVER);
    }

    /**
     * Schedules a task to be called later
     * 
     * @param delay
     *            The amount of ticks to delay the call
     * @param task
     *            The {@link Runnable} to be run when the delay is up
     * @param side
     *            The side to schedule the task on.
     *            <p>
     *            You will get a different {@link TickEvent} depending on the
     *            side. <br>
     *            {@link Side#CLIENT} will be passed a {@link ClientTickEvent} <br>
     *            {@link Side#SERVER} will be passed a {@link ServerTickEvent}
     *            <p>
     *            Note: passing in {@link Side#CLIENT} on a dedicated server
     *            will work, but your task will never be called. Please avoid
     *            doing this to save processing.
     */
    public void schedule(int delay, Runnable task, Side side)
    {
        tasks.add(new Task(delay, task, side));
    }

    /**
     * Returns the {@link Scheduler} instance for the current side.
     * 
     * @see {@link Scheduler#schedule(int, Runnable)} for scheduling
     *      information.
     * 
     * @return The {@link Scheduler} instance.
     */
    public static Scheduler instance()
    {
        return EnderCore.proxy.getScheduler();
    }

    /**
     * For internal use only. Do not call.
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            runTasks(Side.SERVER);
        }
    }

    /**
     * For internal use only. Do not call.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            runTasks(Side.CLIENT);
        }
    }

    private void runTasks(Side side)
    {
        Iterator<Task> iter = tasks.iterator();
        while (iter.hasNext())
        {
            Task next = iter.next();
            if (next.side == side && next.run())
            {
                iter.remove();
            }
        }
    }
}
