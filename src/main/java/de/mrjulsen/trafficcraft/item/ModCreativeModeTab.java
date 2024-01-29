package de.mrjulsen.trafficcraft.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    
    private static final Map<ModTab, Collection<RegistryObject<? extends ItemLike>>> CREATIVE_MODE_TAB_REGISTRY = new HashMap<>();

    private static Map<ModTab, CreativeModeTab> MOD_TAB = new HashMap<>();;

    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        for (ModTab tab : ModTab.values()) {
            MOD_TAB.put(tab, event.registerCreativeModeTab(new ResourceLocation(ModMain.MOD_ID, "trafficcrafttab"), (builder) -> builder
                .icon(() -> new ItemStack(ModBlocks.TRAFFIC_LIGHT.get()))
                .title(Utils.translate("itemGroup.trafficcrafttab"))
            ));        
        }
    }

    public static void put(ModTab tab, RegistryObject<? extends ItemLike> item) {
        if (!CREATIVE_MODE_TAB_REGISTRY.containsKey(tab)) {
            CREATIVE_MODE_TAB_REGISTRY.put(tab, new ArrayList<>());
        }
        CREATIVE_MODE_TAB_REGISTRY.get(tab).add(item);
    }

    public static void addCreative(CreativeModeTabEvent.BuildContents event) {
        event.acceptAll(CREATIVE_MODE_TAB_REGISTRY.entrySet().stream().filter(x -> MOD_TAB.get(x.getKey()) == event.getTab()).findFirst().orElse(null).getValue().stream().map(x -> new ItemStack(x.get())).toList());
    }

    public static enum ModTab {
        MAIN;
    }
}
