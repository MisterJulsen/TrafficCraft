package de.mrjulsen.trafficcraft.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.trafficcraft.TrafficCraft;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.material.MapColor;

/*
 * EXTENDED COPY OF DyeColor.class
 */
public enum PaintColor implements ITranslatableEnum {
	NONE(-1, "none", 0xFFFFFFFF, MapColor.NONE, 0xFFFFFFFF, 0xFFFFFFFF),
	WHITE(0, "white", 16383998, MapColor.SNOW, 15790320, 16777215),
	ORANGE(1, "orange", 16351261, MapColor.COLOR_ORANGE, 15435844, 16738335),
	MAGENTA(2, "magenta", 13061821, MapColor.COLOR_MAGENTA, 12801229, 16711935),
	LIGHT_BLUE(3, "light_blue", 3847130, MapColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
	YELLOW(4, "yellow", 16701501, MapColor.COLOR_YELLOW, 14602026, 16776960),
	LIME(5, "lime", 8439583, MapColor.COLOR_LIGHT_GREEN, 4312372, 12582656),
	PINK(6, "pink", 15961002, MapColor.COLOR_PINK, 14188952, 16738740),
	GRAY(7, "gray", 4673362, MapColor.COLOR_GRAY, 4408131, 8421504),
	LIGHT_GRAY(8, "light_gray", 10329495, MapColor.COLOR_LIGHT_GRAY, 11250603, 13882323),
	CYAN(9, "cyan", 1481884, MapColor.COLOR_CYAN, 2651799, 65535),
	PURPLE(10, "purple", 8991416, MapColor.COLOR_PURPLE, 8073150, 10494192),
	BLUE(11, "blue", 3949738, MapColor.COLOR_BLUE, 2437522, 255),
	BROWN(12, "brown", 8606770, MapColor.COLOR_BROWN, 5320730, 9127187),
	GREEN(13, "green", 6192150, MapColor.COLOR_GREEN, 3887386, 65280),
	RED(14, "red", 11546150, MapColor.COLOR_RED, 11743532, 16711680),
	BLACK(15, "black", 1908001, MapColor.COLOR_BLACK, 1973019, 0);
	
	private final int index;
	private final String name;
	private final int textureColor;
	private final MapColor materialColor;
	private final int fireworkColor;
	private final int textColor;
	
	private PaintColor(int index, String name, int textureColor, MapColor materialColor, int fireworkColor, int textColor) {
		this.index = index;
		this.name = name;
		this.textureColor = textureColor;
		this.materialColor = materialColor;
		this.fireworkColor = fireworkColor;
		this.textColor = textColor;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public DLColor getTextureColor() {
		return DLColor.fromInt(textureColor);
	}

	public MapColor getMaterialColor() {
		return materialColor;
	}

	public int getFireworkColor() {
		return fireworkColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public static PaintColor getByIndex(int index) {
		return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(WHITE);
	}

	public static PaintColor getByDye(DyeItem item) {
		return Arrays.stream(values()).filter(x -> x.getIndex() == item.getDyeColor().getId()).findFirst().orElse(WHITE);
	}

	public static PaintColor getByDye(DyeColor color) {
		return Arrays.stream(values()).filter(x -> x.getIndex() == color.getId()).findFirst().orElse(WHITE);
	}

	/** @retuns true, if color should be white */
	public static boolean useWhiteOrBlackForeColor(int color) {
	   int red = (color >> 16) & 0xFF;
	   int green = (color >> 8) & 0xFF;
	   int blue = color & 0xFF;
 
	   double luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
	   return luminance < 0.5;
	}

	@Override
	public Data getTranslationData() {
		return new Data(TrafficCraft.MOD_ID, "paint_color", name);
	}
}
