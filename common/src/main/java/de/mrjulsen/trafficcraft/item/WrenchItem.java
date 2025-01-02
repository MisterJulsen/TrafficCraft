package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.trafficcraft.registry.ModCreativeModeTab;
import dev.architectury.extensions.injected.InjectedItemPropertiesExtension;
import net.minecraft.world.item.Item;

public class WrenchItem extends Item {

    public WrenchItem() {
        super(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB).stacksTo(1));
    }
    
}
