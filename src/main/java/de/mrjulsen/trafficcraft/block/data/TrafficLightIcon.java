package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightIcon implements StringRepresentable, ITranslatableEnum {
    NONE("none", 0, TrafficLightType.values(), TrafficLightColor.values()),
	RIGHT("right", 1, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.H1_H2_H3_H5 }),
	LEFT("left", 2, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.H1_H2_H3_H5 }),
	STRAIGHT("straight", 3, new TrafficLightType[] { TrafficLightType.CAR, TrafficLightType.TRAM }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN, TrafficLightColor.H1_H2_H3_H5 }),
	STRAIGHT_RIGHT("straight_right", 4, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	STRAIGHT_LEFT("straight_left", 5, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	PEDESTRIAN("pedestrian", 6, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN }),
	BIKE("bike", 7, new TrafficLightType[] { TrafficLightType.CAR }, new TrafficLightColor[] { TrafficLightColor.RED, TrafficLightColor.YELLOW, TrafficLightColor.GREEN });
	
	private String name;
	private byte index;
	private TrafficLightType[] allowedInTypes;
	private TrafficLightColor[] applicableToColors;
	
	private TrafficLightIcon(String name, int index, TrafficLightType[] allowedInTypes, TrafficLightColor[] applicableToColors) {
		this.name = name;
		this.index = (byte)index;
		this.allowedInTypes = allowedInTypes;
		this.applicableToColors = applicableToColors;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
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
