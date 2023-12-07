package de.mrjulsen.trafficcraft;

import java.util.Random;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

public class Constants {
    public static final byte TPS = 1000 / MinecraftServer.MS_PER_TICK;
    public static final int MAX_ASPHALT_PATTERNS = 323;
    public static final int MAX_PAINT = 128;
    public static final int TICKS_PER_DAY = 24000;

    public static final int METAL_COLOR = 0xFF828282;
    public static final int TRAFFIC_CONE_BASE_COLOR = 0xFFD12725;

    public static final MutableComponent CREATIVE_MODE_ONLY_TOOLTIP = GuiUtils.translate("core.trafficcraft.creative_only.tooltip").withStyle(ChatFormatting.GOLD);

    public static final Random RANDOM = new Random();
}
