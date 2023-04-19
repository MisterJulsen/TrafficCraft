package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);


    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench", () -> new WrenchItem());

    public static final RegistryObject<Item> TRAFFIC_LIGHT_LINKER = ITEMS.register("traffic_light_linker", () -> new TrafficLightLinkerItem(new Item.Properties()
        .tab(ModCreativeModeTab.MOD_TAB)
        .stacksTo(1)
    ));

    public static final RegistryObject<Item> BITUMEN = ITEMS.register("raw_bitumen", () -> new Item(new Item.Properties()
        .tab(ModCreativeModeTab.MOD_TAB)
    ));

    public static final RegistryObject<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new BrushItem(new Item.Properties()
        .tab(ModCreativeModeTab.MOD_TAB),
        0
    ));

    /*
    public static final RegistryObject<Item> TRAFFIC_LIGHT_LINKER = ITEMS.register("salt", () -> new Item(new Item.Properties()
        .tab(ModCreativeModeTab.MOD_TAB)
    ));
    */

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);        
    }

}
