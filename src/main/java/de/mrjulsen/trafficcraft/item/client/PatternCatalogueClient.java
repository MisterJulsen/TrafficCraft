package de.mrjulsen.trafficcraft.item.client;

import de.mrjulsen.trafficcraft.screen.TrafficSignPatternSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class PatternCatalogueClient {

    public static void showGui(ItemStack stack) {        
        Minecraft.getInstance().setScreen(new TrafficSignPatternSelectionScreen(stack));
    }
    
}
