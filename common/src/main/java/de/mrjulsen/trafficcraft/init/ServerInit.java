package de.mrjulsen.trafficcraft.init;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.world.ModWorldEvents;
import dev.architectury.event.events.common.LifecycleEvent;

public class ServerInit {

    public static void init() {            
        LifecycleEvent.SETUP.register(() -> {
            TrafficCraft.LOGGER.info("Welcome to the TRAFFICCRAFT mod by MRJULSEN.");
            ModWorldEvents.init();
        });
    }
    
}
