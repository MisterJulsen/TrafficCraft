package de.mrjulsen.trafficcraft.block.client;

import de.mrjulsen.trafficcraft.screen.TrafficLightConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TrafficLightClient {

    public static void showGui(BlockPos pos, Level level) {
        Minecraft.getInstance().setScreen(new TrafficLightConfigScreen(pos, level));
    }

}
