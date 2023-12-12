package de.mrjulsen.trafficcraft.block.data;

import java.util.List;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightColor implements StringRepresentable {
    NONE("none", 0),
	RED("red", 1),
	YELLOW("yellow", 2),
	GREEN("green", 3);
	
	private String name;
	private int index;
	
	private TrafficLightColor(String name, int index) {
		this.name = name;
		this.index = index;
	}
	
	public String getName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public static List<TrafficLightColor> unrenderableColors() {
		return List.of(TrafficLightColor.NONE);
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlightcolor.%s", name);
	}

	public static TrafficLightColor getDirectionByIndex(int index) {
		for (TrafficLightColor shape : TrafficLightColor.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightColor.NONE;
	}

    @Override
    public String getSerializedName() {
        return name;
    }
}
