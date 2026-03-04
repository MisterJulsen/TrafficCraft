package de.mrjulsen.trafficcraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ColorPaletteItem extends Item implements IUseDataComponent<List<Integer>> {

    public static final int MAX_COLORS = 7;
    public static final String COLORS_TAG = "colors";

    public ColorPaletteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (!(stack.getItem() instanceof ColorPaletteItem item)) {
            return;
        }

        if (!item.hasComponent(stack)) {
            tooltipComponents.add(TextUtils.translate("item.trafficcraft.color_palette.no_color").withStyle(ChatFormatting.GRAY));
            return;
        }

        List<Integer> colors = item.getComponent(stack);

        if (!colors.stream().anyMatch(x -> x != 0)) {
            tooltipComponents.add(TextUtils.translate("item.trafficcraft.color_palette.no_color").withStyle(ChatFormatting.GRAY));
        } else {
            for (int i : colors) {
                if (i == 0) {
                    tooltipComponents.add(TextUtils.translate("item.trafficcraft.color_palette.color_unset").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                } else {                
                    DLColor c = DLColor.fromInt(i);
                    tooltipComponents.add(TextUtils.text(String.format("\u2B1B  %s, %s, %s (#%s)", c.getRed(), c.getGreen(), c.getBlue(), Integer.toHexString(i).toUpperCase())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(i))));
                }
            } 
        }
    }

    public static int getColorAt(ItemStack stack, int index) {
        if (!(stack.getItem() instanceof ColorPaletteItem item)) {
            return 0;
        }

        if (!item.hasComponent(stack)) {
            return 0;
        }

        if (index < 0 || index >= MAX_COLORS) {
            return 0;
        }

        return item.getComponent(stack).get(index);
    }

    public static boolean setColor(ItemStack stack, int index, int color) {

        if (!(stack.getItem() instanceof ColorPaletteItem item)) {
            return false;
        }

        if (index < 0 || index >= MAX_COLORS) {
            return false;
        }
        
        List<Integer> colors = new ArrayList<>(item.getComponent(stack));
        colors.set(index, color);
        item.setComponent(stack, colors);
        return true;
    }

    @Override
    public DataComponentType<List<Integer>> getComponentType() {
        return ModDataComponents.COLOR_PALETTE_COMPONENT.get();
    }

    @Override
    public List<Integer> emptyComponent() {
        List<Integer> list =  new ArrayList<>(MAX_COLORS);
        for (int i = 0; i < MAX_COLORS; i++) {
            list.add(0);
        }
        return list;
    }
}