package de.mrjulsen.trafficcraft.neoforge;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.tooltip.ClientTrafficSignTooltipStack;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@Mod.EventBusSubscriber(modid = TrafficCraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

	@SubscribeEvent
	public static void onRegisterTooltipEvent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(TrafficSignTooltip.class, (tooltip) -> {
			return new ClientTrafficSignTooltipStack(tooltip);
		});
	}
}
