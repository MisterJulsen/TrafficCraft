package de.mrjulsen.trafficcraft.util;

import java.io.IOException;
import java.util.Base64;

import com.mojang.blaze3d.platform.NativeImage;

public class Utils {
    public static NativeImage base64ToByteArray(String base64String) throws IOException {
        return NativeImage.read(Base64.getDecoder().decode(base64String));
    }

    private static double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }

    public static double getScale(int fontWidth, int lineWidth, float min, float max) {
        return calcScale(min, max, lineWidth / max, fontWidth);
    }
}
