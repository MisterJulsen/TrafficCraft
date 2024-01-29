package de.mrjulsen.trafficcraft.init;

import de.mrjulsen.trafficcraft.client.ClientSetup;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInitWrapper {
    public static void setup(final FMLClientSetupEvent event) {
        ClientInit.setup(event);
    }

    public static void tooltipSetup(final RegisterClientTooltipComponentFactoriesEvent event) {
        ClientSetup.onRegisterTooltipEvent(event);
    }
}
