package de.mrjulsen.trafficcraft.item;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public interface IScrollEventItem {
    boolean mouseScroll(LocalPlayer player, ItemStack itemStack, double scrollDelta, double mouseX, double mouseY, boolean mouseRightDown, boolean mouseLeftDown, boolean mouseMiddleDown);
}
