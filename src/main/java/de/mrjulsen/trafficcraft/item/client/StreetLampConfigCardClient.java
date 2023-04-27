package de.mrjulsen.trafficcraft.item.client;

import de.mrjulsen.trafficcraft.block.properties.TimeFormat;
import de.mrjulsen.trafficcraft.screen.StreetLampScheduleScreen;
import net.minecraft.client.Minecraft;

public class StreetLampConfigCardClient {

    public static void showGui(int turnOnTime, int turnOfftime, TimeFormat format) {        
        Minecraft.getInstance().setScreen(new StreetLampScheduleScreen(turnOnTime, turnOfftime, format));
    }
    
}
