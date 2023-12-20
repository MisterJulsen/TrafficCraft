package de.mrjulsen.trafficcraft.init;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerInit {

    public static void setup(FMLCommonSetupEvent event) {            
        ModMain.LOGGER.info("Welcome to the TRAFFICCRAFT mod by MRJULSEN.");
    }
    
}
