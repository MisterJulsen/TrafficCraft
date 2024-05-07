package de.mrjulsen.legacydragonlib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

import com.google.gson.Gson;

import de.mrjulsen.legacydragonlib.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class DragonLibConstants {
    public static final String DRAGONLIB_MODID = "trafficcraft";

    public static final String CONFIG_DIR = "./config";
    
    public static final int TICKS_PER_DAY = Level.TICKS_PER_DAY;
    public static final byte TPS = 1000 / MinecraftServer.MS_PER_TICK;
	/** float size of one pixel. */ public static final float PIXEL = 1.0F / 16.0F;

    public static final ResourceLocation UI = new ResourceLocation(DRAGONLIB_MODID, "textures/gui/ui.png");

    public static final int DEFAULT_UI_FONT_COLOR = 4210752;
    
    /** HERE BE DRAGONS! 🐉 */ public static final Component TEXT_DRAGON = Utils.translate("text." + DRAGONLIB_MODID + ".dragon");
    public static final Component TEXT_NEXT = Utils.translate("text." + DRAGONLIB_MODID + ".next");
    public static final Component TEXT_PREVIOUS = Utils.translate("text." + DRAGONLIB_MODID + ".previous");
    public static final Component TEXT_GO_BACK = Utils.translate("text." + DRAGONLIB_MODID + ".go_back");
    public static final Component TEXT_GO_FORTH = Utils.translate("text." + DRAGONLIB_MODID + ".go_forth");    
    public static final Component TEXT_GO_UP = Utils.translate("text." + DRAGONLIB_MODID + ".go_down");
    public static final Component TEXT_GO_DOWN = Utils.translate("text." + DRAGONLIB_MODID + ".go_up");
    public static final Component TEXT_GO_RIGHT= Utils.translate("text." + DRAGONLIB_MODID + ".go_right");
    public static final Component TEXT_GO_LEFT = Utils.translate("text." + DRAGONLIB_MODID + ".go_left");
    public static final Component TEXT_GO_TO_TOP = Utils.translate("text." + DRAGONLIB_MODID + ".go_to_top");
    public static final Component TEXT_GO_TO_BOTTOM = Utils.translate("text." + DRAGONLIB_MODID + ".go_to_bottom");
    public static final Component TEXT_RESET_DEFAULTS = Utils.translate("text." + DRAGONLIB_MODID + ".reset_defaults");
    public static final Component TEXT_EXPAND = Utils.translate("text." + DRAGONLIB_MODID + ".expand");
    public static final Component TEXT_COLLAPSE = Utils.translate("text." + DRAGONLIB_MODID + ".collapse");
    public static final Component TEXT_COUNT = Utils.translate("text." + DRAGONLIB_MODID + ".count");
    public static final Component TEXT_TRUE = Utils.translate("text." + DRAGONLIB_MODID + ".true");
    public static final Component TEXT_FALSE = Utils.translate("text." + DRAGONLIB_MODID + ".false");
    public static final Component TEXT_CLOSE = Utils.translate("text." + DRAGONLIB_MODID + ".close");
    public static final Component TEXT_SHOW = Utils.translate("text." + DRAGONLIB_MODID + ".show");
    public static final Component TEXT_HIDE = Utils.translate("text." + DRAGONLIB_MODID + ".hide");
    public static final Component TEXT_SEARCH = Utils.translate("text." + DRAGONLIB_MODID + ".search");
    public static final Component TEXT_REFRESH = Utils.translate("text." + DRAGONLIB_MODID + ".refresh");
    public static final Component TEXT_RELOAD = Utils.translate("text." + DRAGONLIB_MODID + ".reload");

    public static final Random RANDOM = new Random();
    public static final Gson GSON = new Gson();
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();
}
