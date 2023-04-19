package de.mrjulsen.trafficcraft.item.client;

import de.mrjulsen.trafficcraft.screen.PaintBrushScreen;
import net.minecraft.client.Minecraft;

public class BrushClient {

    public static void showGui(int pattern, int paint, int color, float scroll) {        
        Minecraft.getInstance().setScreen(new PaintBrushScreen(pattern, paint, color, scroll));
    }
    
}
