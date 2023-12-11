package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
	public static void onTick(ClientTickEvent event) {
        RoadConstructionTool.clientTick();
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            return;
        }

        TrafficLightTextureManager.getAllTextureLocations().forEach(x -> event.addSprite(x));
    }
}
