package de.mrjulsen.trafficcraft.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IScrollEventItem {
    boolean mouseScroll(Player player, ItemStack itemStack, double scrollDelta, double mouseX, double mouseY, boolean mouseRightDown, boolean mouseLeftDown, boolean mouseMiddleDown);
}
