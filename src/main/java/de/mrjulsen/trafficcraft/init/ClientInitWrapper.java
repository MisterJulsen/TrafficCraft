package de.mrjulsen.trafficcraft.init;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInitWrapper {
    public static void setup(final FMLClientSetupEvent event) {
        ClientInit.setup(event);
    }
}
