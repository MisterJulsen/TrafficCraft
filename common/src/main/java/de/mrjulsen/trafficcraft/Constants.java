package de.mrjulsen.trafficcraft;

import java.util.Random;

import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

public class Constants {
    public static final int MAX_ASPHALT_PATTERNS = 323;
    public static final int MAX_PAINT = 128;

    public static final DLColor METAL_COLOR = DLColor.fromInt(0xFF828282);
    public static final DLColor TRAFFIC_CONE_BASE_COLOR = DLColor.fromInt(0xFFD12725);

    public static final MutableComponent CREATIVE_MODE_ONLY_TOOLTIP = TextUtils.translate("core.trafficcraft.creative_only.tooltip").withStyle(ChatFormatting.GOLD);
    
    public static final Component textCopy = TextUtils.translate("core.trafficcraft.common.copy");
    public static final Component textPaste = TextUtils.translate("core.trafficcraft.common.paste");

    public static final Random RANDOM = new Random();
    public static final RandomSource RANDOM_SOURCE = RandomSource.create();
}


