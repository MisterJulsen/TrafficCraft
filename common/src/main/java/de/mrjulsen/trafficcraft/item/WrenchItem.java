package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.trafficcraft.registry.ModCreativeModeTab;
import net.minecraft.world.item.Item;

public class WrenchItem extends Item {

    public WrenchItem() {
        super(new Properties().tab(ModCreativeModeTab.MOD_TAB).stacksTo(1));
    }
    
}
