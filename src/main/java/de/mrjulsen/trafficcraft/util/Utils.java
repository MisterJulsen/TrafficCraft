package de.mrjulsen.trafficcraft.util;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;

public class Utils {
    public static void giveAdvancement(ServerPlayer player, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(ModMain.MOD_ID, name));
        player.getAdvancements().award(adv, criteriaKey);
    }

    @SuppressWarnings("resource")
    public static <W extends AbstractWidget> void renderTooltip(Screen s, W w, Supplier<List<FormattedCharSequence>> lines, PoseStack stack, int mouseX, int mouseY) {
        if (w.isMouseOver(mouseX, mouseY)) {
            s.renderTooltip(stack, lines.get(), mouseX, mouseY, s.getMinecraft().font);
        }
    }

    public static int coordsToInt(byte x, byte y) {
        int coords = ((x & 0xFF) << 16) | (y & 0xFF);
        return coords;
    }
    
    public static byte[] intToCoords(int coords) {
        byte x = (byte) ((coords >> 16) & 0xFF);
        byte y = (byte) (coords & 0xFF);
        return new byte[] {x, y};
    }

    public static int swapRedBlue(int argbColor) {
        int alpha = (argbColor >> 24) & 0xFF;
        int red = (argbColor >> 16) & 0xFF;
        int green = (argbColor >> 8) & 0xFF;
        int blue = argbColor & 0xFF;
    
        int bgrColor = (alpha << 24) | (blue << 16) | (green << 8) | red;
    
        return bgrColor;
    }
}
