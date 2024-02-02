package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.client.tooltip.ClientTrafficSignTooltipStack;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onRegisterTooltipEvent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(TrafficSignTooltip.class, (tooltip) -> {
            return new ClientTrafficSignTooltipStack(tooltip);
        });
	}

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(new TintedTextures.TintedBlock(), ModBlocks.COLORED_BLOCKS.stream().map(x -> x.get()).toArray(Block[]::new));
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        List<ItemLike> items = new ArrayList<>(ModItems.COLORED_ITEMS.stream().map(x -> x.get()).toList());
        items.addAll(ModBlocks.COLORED_BLOCKS.stream().map(x -> x.get()).toList());        
        event.register(new TintedTextures.TintedItem(), items.toArray(ItemLike[]::new));
    }
}
