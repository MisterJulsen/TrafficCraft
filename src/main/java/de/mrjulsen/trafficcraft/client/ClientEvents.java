package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
	public static void onTick(ClientTickEvent event) {
        RoadConstructionTool.clientTick();
    }
}
