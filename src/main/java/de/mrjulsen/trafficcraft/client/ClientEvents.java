package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.trafficcraft.item.IScrollEventItem;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
	public static void onTick(ClientTickEvent event) {
        RoadConstructionTool.clientTick();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("resource")
    public static void mouseScrollEvent(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        double scroll = event.getScrollDelta();

        if (player == null || scroll == 0) {
            return;
        }

        ItemStack stack = player.getMainHandItem() == null ? player.getOffhandItem() : player.getMainHandItem();
        if (stack != null && stack.getItem() instanceof IScrollEventItem item) {
            event.setCanceled(item.mouseScroll(player, stack, scroll, event.getMouseX(), event.getMouseY(), event.isRightDown(), event.isLeftDown(), event.isMiddleDown()));
        }
    }
}
