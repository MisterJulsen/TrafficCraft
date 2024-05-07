package de.mrjulsen.legacydragonlib.internal;

import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class DragonLibClientProxy {

    public static void setup(final FMLClientSetupEvent event) {
        GuiUtils.init();
    }
    
}
