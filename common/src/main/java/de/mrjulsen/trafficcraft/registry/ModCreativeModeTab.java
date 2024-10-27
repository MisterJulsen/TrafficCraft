package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {

    public static final CreativeModeTab MOD_TAB = CreativeTabRegistry.create(new ResourceLocation(TrafficCraft.MOD_ID, "trafficcrafttab"), () -> new ItemStack(ModBlocks.TRAFFIC_LIGHT.get()));
    public static void init() { } 

}
