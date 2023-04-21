package de.mrjulsen.trafficcraft.block.client;

import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.screen.TrafficSignEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TrafficSignClient {
    
    public static void showGui(int pattern, float scroll, TrafficSignShape shape, BlockPos pos, Level level, Player player) {
        Minecraft.getInstance().setScreen(new TrafficSignEditScreen(pattern, scroll, shape, pos, level, player));
    }

}
