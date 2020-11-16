package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.enderio.core.EnderCore;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Convenience helper to schedule events to happen in the future.
 */
@Mod.EventBusSubscriber
public class Scheduler {

  /**
   * A task to be executed later.
   *
   */
  public static interface ITask {
    /**
     * This method will be called at the next post tick event. It can return a new task (or itself) to be enqueued again.
     *
     * @return null or a task to be enqueued
     */
    ITask run();
  }

  private static final class Task implements ITask {
    private int delay;
    private Runnable toRun;

    Task(int delay, Runnable toRun) {
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
   * @param delay
   *          The amount of ticks to delay the call
   * @param task
   *          The {@link Runnable} to be run when the delay is up
   *
   * @see #schedule(int, Runnable, Dist)
   */
  public void schedule(int delay, Runnable task) {
    schedule(delay, task, Dist.DEDICATED_SERVER);
  }


  public void schedule(int delay, Runnable task, Dist side) {
    schedule(new Task(delay, task), side);
  }


  public void schedule(ITask task, Dist side) {
    if (side == Dist.DEDICATED_SERVER) {
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
  public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      runTasks(serverQueue);
    }
  }

  /**
   * For internal use only. Do not call.
   */
  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      runTasks(clientQueue);
      if (!serverQueue.isEmpty() && !Minecraft.getInstance().isIntegratedServerRunning()) {
        serverQueue.clear();
      }
    }
  }

  private static void runTasks(Queue<ITask> queue) {
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
