package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {


    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.CREATIVE_MODE_TAB);
    
    public static final RegistrySupplier<CreativeModeTab> MOD_TAB = TABS.register(ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "trafficcrafttab"), 
            () -> CreativeTabRegistry.create(
                    TextUtils.translate("itemGroup.trafficcraft.trafficcrafttab"),
                    () -> new ItemStack(ModBlocks.TRAFFIC_LIGHT.get())
            )
    );

    public static void init() {
        TABS.register();
    } 

}
