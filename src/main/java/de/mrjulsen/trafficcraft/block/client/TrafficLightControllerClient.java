package de.mrjulsen.trafficcraft.block.client;

import de.mrjulsen.trafficcraft.screen.TrafficLightControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TrafficLightControllerClient {

    public static void showGui(BlockPos pos, Level level) {
        Minecraft.getInstance().setScreen(new TrafficLightControllerScreen(pos, level));
    }

}
