package com.enderio.core.client;

import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.Scheduler;

public class ClientProxy extends CommonProxy
{
    private static final Scheduler scheduler = new Scheduler();
    
    @Override
    public Scheduler getScheduler()
    {
        return scheduler;
    }
}
