package de.mrjulsen.trafficcraft.item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import de.mrjulsen.mcdragonlib.client.ColorObject;
import de.mrjulsen.mcdragonlib.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ColorPaletteItem extends Item {

    public static final int MAX_COLORS = 7;
    public static final String COLORS_TAG = "colors";

    public ColorPaletteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        if (!stack.hasTag()) {
            list.add(Utils.translate("item.trafficcraft.color_palette.no_color").withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag tag = checkNbt(stack);

        if (!IntStream.of(tag.getIntArray(COLORS_TAG)).anyMatch(x -> x != 0)) {
            list.add(Utils.translate("item.trafficcraft.color_palette.no_color").withStyle(ChatFormatting.GRAY));
        } else {
            for (int i : tag.getIntArray(COLORS_TAG)) {
                if (i == 0) {
                    list.add(Utils.translate("item.trafficcraft.color_palette.color_unset").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                } else {                
                    ColorObject c = ColorObject.fromInt(i);
                    list.add(Utils.text(String.format("\u2B1B  %s, %s, %s (#%s)", c.getR(), c.getG(), c.getB(), Integer.toHexString(i).toUpperCase())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(i))));
                }
            } 
        }
    }

    public static CompoundTag checkNbt(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(COLORS_TAG)) {
            int[] c = new int[7];
            Arrays.fill(c, 0);
            nbt.putIntArray(COLORS_TAG, c);
        }

        return nbt;
    }

    public static int getColorAt(ItemStack stack, int index) {
        if (!stack.hasTag()) {
            return 0;
        }

        if (index < 0 || index >= MAX_COLORS) {
            return 0;
        }

        return checkNbt(stack).getIntArray(COLORS_TAG)[index];
    }

    public static boolean setColor(ItemStack stack, int index, int color) {
        if (index < 0 || index >= MAX_COLORS) {
            return false;
        }
        
        int[] a = checkNbt(stack).getIntArray(COLORS_TAG);
        a[index] = color;
        checkNbt(stack).putIntArray(COLORS_TAG, a);
        return true;
    }
}