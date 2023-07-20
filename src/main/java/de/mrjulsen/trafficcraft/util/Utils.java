package de.mrjulsen.trafficcraft.util;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class Utils {
     public static void giveAdvancement(ServerPlayer player, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(ModMain.MOD_ID, name));
        player.getAdvancements().award(adv, criteriaKey);
    }
}
