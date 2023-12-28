package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightIcon implements StringRepresentable, ITranslatableEnum, IIconEnum {
    NONE("none", 0, 0, 1, TrafficLightType.values(), TrafficLightColor.values()),
	RIGHT("right", 1, 1, 1, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.F1_F2_F3_F5 }),
	LEFT("left", 2, 2, 1, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.F1_F2_F3_F5 }),
	STRAIGHT("straight", 3, 3, 1, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.F1_F2_F3_F5 }),
	STRAIGHT_RIGHT("straight_right", 4, 4, 1, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	STRAIGHT_LEFT("straight_left", 5, 5, 1, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	PEDESTRIAN("pedestrian", 6, 6, 1, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	BIKE("bike", 7, 7, 1, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN });
	
	private String name;
	private byte index;
	private int uMul;
	private int vMul;
	private TrafficLightType[] allowedInTypes;
	private TrafficLightColor[] applicableToColors;
	
	private TrafficLightIcon(String name, int index, int u, int v, TrafficLightType[] allowedInTypes, TrafficLightColor[] applicableToColors) {
		this.name = name;
		this.index = (byte)index;
		this.uMul = u;
		this.vMul = v;
		this.allowedInTypes = allowedInTypes;
		this.applicableToColors = applicableToColors;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	@Override
	public int getUMultiplier() {
		return uMul;
	}

	@Override
	public int getVMultiplier() {
		return vMul;
	}

	public static TrafficLightIcon[] getAllowedForType(TrafficLightType type) {
		return Arrays.stream(TrafficLightIcon.values()).filter(x -> Arrays.stream(x.allowedInTypes).anyMatch(y -> y == type)).toArray(TrafficLightIcon[]::new);
	}

	public boolean isAllowedFor(TrafficLightType type) {
		return Arrays.stream(allowedInTypes).anyMatch(x -> x == type);
	}

	public static TrafficLightIcon[] applicableToColor(TrafficLightColor color) {
		return Arrays.stream(TrafficLightIcon.values()).filter(x -> Arrays.stream(x.applicableToColors).anyMatch(y -> y == color)).toArray(TrafficLightIcon[]::new);
	}

	public boolean isApplicableToColor(TrafficLightColor color) {
		return Arrays.stream(applicableToColors).anyMatch(x -> x == color);
	} 

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlighticon.%s", name);
	}

	public static TrafficLightIcon getIconByIndex(byte index) {
		return Arrays.stream(TrafficLightIcon.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightIcon.NONE);
	}

	public Sprite getSprite(TrafficLightType type) {
		return new Sprite(ICON_TEXTURE_LOCATION, TEXTURE_SIZE, TEXTURE_SIZE, DEFAULT_SPRITE_SIZE * getUMultiplier(), DEFAULT_SPRITE_SIZE * (getVMultiplier() + type.getIndex()), DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE);
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getEnumName() {
		return "trafficlighticon";
	}

	@Override
	public String getEnumValueName() {
		return getName();
	}
}
