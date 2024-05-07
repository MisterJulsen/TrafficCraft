package de.mrjulsen.legacydragonlib.internal.content;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DragonLibItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DragonLibConstants.DRAGONLIB_MODID);


    public static final RegistryObject<Item> DRAGON = ITEMS.register("dragon_item", () -> new Item(new Item.Properties())); 

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);        
    }

}

