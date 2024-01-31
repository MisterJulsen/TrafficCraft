package de.mrjulsen.trafficcraft.util;

import java.io.IOException;
import java.util.Base64;

import com.mojang.blaze3d.platform.NativeImage;

public class Utils {
    public static NativeImage base64ToByteArray(String base64String) throws IOException {
        return NativeImage.read(Base64.getDecoder().decode(base64String));
    }
}
