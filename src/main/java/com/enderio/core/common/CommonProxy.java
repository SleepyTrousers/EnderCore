package com.enderio.core.common;

import com.enderio.core.common.util.Scheduler;

public class CommonProxy
{
    private static final Scheduler scheduler = new Scheduler();

    /**
     * Returns a scheduler for the current side
     * <p>
     * For internal use only, please call {@link Scheduler#instance()} to obtain an
     * {@link Scheduler} instance.
     */
    public Scheduler getScheduler()
    {
        return scheduler;
    }
}
