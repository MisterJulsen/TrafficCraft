package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightColor implements StringRepresentable {
    NONE("none", 0, TrafficLightType.values(), 0),
	RED("red", 1, new TrafficLightType[] { TrafficLightType.CAR }, 1),
	YELLOW("yellow", 2, new TrafficLightType[] { TrafficLightType.CAR }, 2),
	GREEN("green", 3, new TrafficLightType[] { TrafficLightType.CAR }, 3),	
	H0("h0", 4, new TrafficLightType[] { TrafficLightType.TRAM }, 1),
	H1_H2_H3_H5("h1_h2_h3_h5", 5, new TrafficLightType[] { TrafficLightType.TRAM }, 3),
	H4("h4", 6, new TrafficLightType[] { TrafficLightType.TRAM }, 2);
	
	private String name;
	private byte index;
	private TrafficLightType[] allowedInTypes;
	private byte groupIndex;
	
	private TrafficLightColor(String name, int index, TrafficLightType[] allowedInTypes, int groupIndex) {
		this.name = name;
		this.index = (byte)index;
		this.allowedInTypes = allowedInTypes;
		this.groupIndex = (byte)groupIndex;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public static List<TrafficLightColor> unrenderableColors() {
		return List.of(TrafficLightColor.NONE);
	}

	public static TrafficLightColor[] getAllowedForType(TrafficLightType type) {
		return Arrays.stream(TrafficLightColor.values()).filter(x -> Arrays.stream(x.allowedInTypes).anyMatch(y -> y == type)).toArray(TrafficLightColor[]::new);
	}

	public boolean isAllowedFor(TrafficLightType type) {
		return Arrays.stream(allowedInTypes).anyMatch(x -> x == type);
	}

	public byte getGroupIndex() {
		return groupIndex;
	}

	/**
	 * Returns an array of {@code TrafficLightColor}s which have a similar meaning. For example: Red and H0 (both mean "stop")
	 */
	public TrafficLightColor[] getSimilar() {
		return Arrays.stream(TrafficLightColor.values()).filter(x -> x.getGroupIndex() == this.getGroupIndex()).toArray(TrafficLightColor[]::new);
	}

	public boolean isSimilar(TrafficLightColor other) {
		return getGroupIndex() == other.getGroupIndex();
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlightcolor.%s", name);
	}

	public static TrafficLightColor getDirectionByIndex(byte index) {
		return Arrays.stream(TrafficLightColor.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightColor.NONE);
	}

    @Override
    public String getSerializedName() {
        return name;
    }
}
