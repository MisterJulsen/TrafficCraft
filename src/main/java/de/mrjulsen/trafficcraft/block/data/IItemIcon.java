package de.mrjulsen.trafficcraft.block.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IItemIcon {
    ItemLike getItemIcon();
    default ItemStack getIconStack() {
        return new ItemStack(getItemIcon());
    }
}
