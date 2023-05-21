package de.mrjulsen.trafficcraft.block.client;

import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.screen.WritableSignScreen;
import net.minecraft.client.Minecraft;

public class WritableTrafficSignClient {
    
    public static void showGui(WritableTrafficSignBlockEntity pSign) {
        Minecraft.getInstance().setScreen(new WritableSignScreen(pSign));
    }

}
