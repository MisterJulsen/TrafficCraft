package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.trafficcraft.data.TrafficSignData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PatternCatalogueItem extends Item {

    public static final int MAX_SIGN_PATTERNS = 16;

    public PatternCatalogueItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static CompoundTag checkNbt(ItemStack stack) {
        CompoundTag nbt;

        if (stack.hasTag()) {
            nbt = stack.getTag();
        } else {
            nbt = new CompoundTag();
            nbt.putInt("selectedIndex", 0);
            nbt.put("patterns", new ListTag());            
        }

        return nbt;
    }

    private static boolean indexInBounds(ItemStack stack, int index) {
        return index < 0 || index >= getStoredPatternCount(stack);
    }

    public static int getSelectedIndex(ItemStack stack) {
        return checkNbt(stack).getInt("selectedIndex");
    }

    public static byte getStoredPatternCount(ItemStack stack) {
        return (byte)checkNbt(stack).getList("patterns", 10).size();
    }

    public static TrafficSignData getPatternAt(ItemStack stack, int index) {
        if (!indexInBounds(stack, index))
            return null;

        return TrafficSignData.fromNbt(checkNbt(stack).getList("patterns", 10).getCompound(index));
    }

    public static TrafficSignData getSelectedPattern(ItemStack stack) {
        return getPatternAt(stack, getSelectedIndex(stack));
    }

    public static TrafficSignData[] getStoredPatterns(ItemStack stack) {
        return checkNbt(stack).getList("patterns", 10).stream().map(x -> {
            return TrafficSignData.fromNbt((CompoundTag)x);
        }).toArray(TrafficSignData[]::new);
    }

    public static boolean setPattern(ItemStack stack, TrafficSignData pattern) {
        if (getStoredPatternCount(stack) >= MAX_SIGN_PATTERNS)
            return false;

        checkNbt(stack).getList("patterns", 10).add(pattern.toNbt());
        return true;
    }

    public static boolean removePatternAt(ItemStack stack, int index) {
        if (!indexInBounds(stack, index))
            return false;

        checkNbt(stack).getList("patterns", 10).remove(index);
        return true;
    }

    public static void clearPatterns(ItemStack stack) {
        checkNbt(stack).getList("patterns", 10).clear();
    }

    public static void setSelectedIndex(ItemStack stack, int index) {
        checkNbt(stack).putInt("selectedIndex", Mth.clamp(index, 0, MAX_SIGN_PATTERNS - 1));
    }
}