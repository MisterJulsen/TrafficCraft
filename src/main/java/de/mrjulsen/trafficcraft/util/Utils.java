package de.mrjulsen.trafficcraft.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.client.widgets.GuiAreaDefinition;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

public class Utils {
    public static void giveAdvancement(ServerPlayer player, String name, String criteriaKey) {
        Advancement adv = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(ModMain.MOD_ID, name));
        player.getAdvancements().award(adv, criteriaKey);
    }

    @SuppressWarnings("resource")
    public static List<FormattedCharSequence> getTooltipData(Screen s, Component c, int maxWidth) {
        return s.getMinecraft().font.split(c, maxWidth);
    }

    @SuppressWarnings("resource")
    public static <W extends AbstractWidget> void renderTooltip(Screen s, W w, Supplier<List<FormattedCharSequence>> lines, PoseStack stack, int mouseX, int mouseY) {
        if (w.isMouseOver(mouseX, mouseY)) {
            
            s.renderTooltip(stack, lines.get(), mouseX, mouseY, s.getMinecraft().font);
        }
    }

    @SuppressWarnings("resource")
    public static boolean renderTooltip(Screen screen, GuiAreaDefinition area, List<FormattedCharSequence> lines, PoseStack stack, int mouseX, int mouseY) {
        if (area.isInBounds(mouseX, mouseY)) {
            screen.renderTooltip(stack, lines, mouseX, mouseY, screen.getMinecraft().font);
			return true;
        }
		return false;
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

    public static String textureToBase64(NativeImage image) {
        try {
            return Base64.encodeBase64String(image.asByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static InputStream scaleImage(InputStream inputStream, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return new ByteArrayInputStream(imageBytes);
    }

    public static long encodeCoordinates(int x, int y, int z) {
        long encodedValue = 0;

        encodedValue |= ((long)x & 0xFFFFFFFFL) << 32;
        encodedValue |= ((long)y & 0xFFFFFFFFL) << 16;
        encodedValue |= ((long)z & 0xFFFFFFFFL);

        return encodedValue;
    }

    public static int[] decodeCoordinates(long encodedValue) {
        int x = (int) (encodedValue >> 32);
        int y = (int) ((encodedValue >> 16) & 0xFFFF);
        int z = (int) (encodedValue & 0xFFFF);

        return new int[]{x, y, z};
    }

    public static double slopeStrength(Vec3 a, Vec3 b) {
        double heightDiff = Math.max(a.y, b.y) - Math.min(a.y, b.y);
        Vec3 vec = b.subtract(a);
        double distance = vec.horizontalDistance();
        return distance / heightDiff;
    }
}
