package de.mrjulsen.trafficcraft;

import java.util.Random;

import de.mrjulsen.mcdragonlib.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Constants {
    public static final int MAX_ASPHALT_PATTERNS = 323;
    public static final int MAX_PAINT = 128;

    public static final int METAL_COLOR = 0xFF828282;
    public static final int TRAFFIC_CONE_BASE_COLOR = 0xFFD12725;

    public static final MutableComponent CREATIVE_MODE_ONLY_TOOLTIP = Utils.translate("core.trafficcraft.creative_only.tooltip").withStyle(ChatFormatting.GOLD);
    
    public static final Component textCopy = Utils.translate("core.trafficcraft.common.copy");
    public static final Component textPaste = Utils.translate("core.trafficcraft.common.paste");

    public static final String GERMAN_LOCAL_CODE = "de";

    public static final String WIKIPEDIA_TRAFFIC_LIGHT_ID = "Q8004";
    public static final String WIKIPEDIA_GERMAN_TRAM_SIGNAL_ID = "Q2354774";

    public static final Random RANDOM = new Random();
}


