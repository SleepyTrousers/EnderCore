package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
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
@Handler(getInstFrom = Inst.METHOD)
public class Scheduler {

  private static final class Task {
    private int delay;
    private Runnable toRun;
    private Side side;

    private Task(int delay, Runnable toRun, Side side) {
      this.delay = delay;
      this.toRun = toRun;
      this.side = side;
    }
    
    private boolean run() {
      if (delay <= 0) {
        toRun.run();
        return true;
      }
      delay--;
      return false;
    }
  }

  private final List<Task> tasks = Collections.synchronizedList(new ArrayList<Task>());

  /**
   * Schedules a task to be called later
   * 
   * @param delay
   *          The amount of ticks to delay the call
   * @param task
   *          The {@link Runnable} to be run when the delay is up
   * 
   * @see #schedule(int, Runnable, Side)
   */
  public void schedule(int delay, Runnable task) {
    schedule(delay, task, Side.SERVER);
  }

  /**
   * Schedules a task to be called later
   * 
   * @param delay
   *          The amount of ticks to delay the call
   * @param task
   *          The {@link Runnable} to be run when the delay is up
   * @param side
   *          The side to schedule the task on.
   *          <p>
   *          You will get a different {@link TickEvent} depending on the side.
   *          <br>
   *          {@link Side#CLIENT} will be passed a {@link ClientTickEvent} <br>
   *          {@link Side#SERVER} will be passed a {@link ServerTickEvent}
   *          <p>
   *          Note: passing in {@link Side#CLIENT} on a dedicated server will
   *          work, but your task will never be called. Please avoid doing this
   *          to save processing.
   */
  public void schedule(int delay, Runnable task, Side side) {
    tasks.add(new Task(delay, task, side));
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
      runTasks(Side.SERVER);
    }
  }

  /**
   * For internal use only. Do not call.
   */
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onClientTick(ClientTickEvent event) {
    if (event.phase == Phase.END) {
      runTasks(Side.CLIENT);
    }
  }

  private void runTasks(Side side) {
    Iterator<Task> iter = tasks.iterator();
    while (iter.hasNext()) {
      Task next = iter.next();
      if (next.side == side && next.run()) {
        iter.remove();
      }
    }
  }
}
