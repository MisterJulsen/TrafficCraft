package de.mrjulsen.trafficcraft.item;

import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ColorPaletteItem extends Item {

    public static final int MAX_COLORS = 7;

    public ColorPaletteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static CompoundTag checkNbt(ItemStack stack) {
        CompoundTag nbt;

        if (stack.hasTag()) {
            nbt = stack.getTag();
        } else {
            nbt = new CompoundTag();
            int[] colors = new int[MAX_COLORS];
            Arrays.fill(colors, 0xFFFFFFFF);
            nbt.putIntArray("colors", colors);
        }

        return nbt;
    }

    public static int getColorAt(ItemStack stack, int index) {
        if (index < 0 || index >= MAX_COLORS) {
            return 0xFFFFFFFF;
        }

        return checkNbt(stack).getIntArray("colors")[index];
    }

    public static boolean setColor(ItemStack stack, int index, int color) {
        if (index < 0 || index >= MAX_COLORS) {
            return false;
        }

        checkNbt(stack).getIntArray("colors")[index] = color;
        return true;
    }
}