package de.mrjulsen.trafficcraft.proxy;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy {

    public static void setup(FMLCommonSetupEvent event) {            
        ModMain.LOGGER.info("Welcome to the TRAFFICCRAFT mod by MRJULSEN.");
    }
    
}
