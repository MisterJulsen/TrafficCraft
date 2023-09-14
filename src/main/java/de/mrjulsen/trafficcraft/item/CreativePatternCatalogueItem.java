package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.client.PatternCatalogueClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CreativePatternCatalogueItem extends PatternCatalogueItem {

    public CreativePatternCatalogueItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxPatterns() {
        return Short.MAX_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        list.add(Constants.CREATIVE_MODE_ONLY_TOOLTIP);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
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

    public static void setCustomImage(ItemStack stack, TrafficSignData data) {        
        checkNbt(stack).put("custom", data.toNbt());
    }

    public static void clearCustomImage(ItemStack stack) { 
        checkNbt(stack).remove("custom");
    }

    public static TrafficSignData getCustomImage(ItemStack stack) {
        if (hasCustomPattern(stack)) {
            return TrafficSignData.fromNbt(checkNbt(stack).getCompound("custom"));
        } else {
            return null;
        }
    }

    public static boolean hasCustomPattern(ItemStack stack) {
        return checkNbt(stack).contains("custom");
    }

    public static boolean shouldUseCustomPattern(ItemStack stack) {
        return checkNbt(stack).contains("custom") && !indexInBounds(stack, getSelectedIndex(stack));
    }
}