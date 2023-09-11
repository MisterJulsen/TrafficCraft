package de.mrjulsen.trafficcraft.item;

import java.util.Arrays;
import java.util.Optional;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.client.PatternCatalogueClient;
import de.mrjulsen.trafficcraft.screen.TrafficSignTooltip;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PatternCatalogueItem extends Item {

    private static final int MAX_SIGN_PATTERNS = 36;

    public PatternCatalogueItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            if (player.isShiftKeyDown()) {
                PatternCatalogueClient.showGui(stack);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public int getMaxPatterns() {
        return MAX_SIGN_PATTERNS;
    }

    public static CompoundTag checkNbt(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains("patterns")) {            
            nbt.put("patterns", new ListTag());
        }

        if (!nbt.contains("selectedIndex")) {
            nbt.putInt("selectedIndex", 0);
        }

        return nbt;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (!pStack.hasTag()) {
            return Optional.of(new TrafficSignTooltip(NonNullList.create(), getSelectedIndex(pStack)));
        }

        NonNullList<TrafficSignData> nonnulllist = NonNullList.create();
        Arrays.stream(getStoredPatterns(pStack)).forEach(nonnulllist::add);
        return Optional.of(new TrafficSignTooltip(nonnulllist, getSelectedIndex(pStack)));
    }

    protected static boolean indexInBounds(ItemStack stack, int index) {
        return index >= 0 && index < getStoredPatternCount(stack);
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
        try (pattern) {
            if (getStoredPatternCount(stack) >= ((PatternCatalogueItem)stack.getItem()).getMaxPatterns())
                return false;

            checkNbt(stack).getList("patterns", 10).add(pattern.toNbt());
            return true;
        }
    }

    public static boolean replacePattern(ItemStack stack, TrafficSignData pattern, int index) {
        try (pattern) {
            if (getStoredPatternCount(stack) >= ((PatternCatalogueItem)stack.getItem()).getMaxPatterns())
            return false;
            checkNbt(stack).getList("patterns", 10).set(index, pattern.toNbt());
            
            return true;
        }
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
        checkNbt(stack).putInt("selectedIndex", Mth.clamp(index, -1, ((PatternCatalogueItem)stack.getItem()).getMaxPatterns() - 1));
    }
}