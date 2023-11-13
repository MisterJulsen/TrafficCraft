package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {
    
    public static final CreativeModeTab MOD_TAB = new CreativeModeTab("trafficcrafttab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.TRAFFIC_LIGHT.get());
        };
    };

}
