package de.mrjulsen.trafficcraft.block.data;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightMode implements StringRepresentable {
    ALL_ON("all", 0),
	OFF("off", 1),
	RED("red", 2),
	RED_YELLOW("red_yellow", 3),
	YELLOW("yellow", 4),
	GREEN("green", 5);
	
	private String mode;
	private int index;
	
	private TrafficLightMode(String shape, int index) {
		this.mode = shape;
		this.index = index;
	}
	
	public String getMode() {
		return this.mode;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.mode.%s", mode);
	}

	public static TrafficLightMode getModeByIndex(int index) {
		for (TrafficLightMode shape : TrafficLightMode.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightMode.OFF;
	}

    @Override
    public String getSerializedName() {
        return mode;
    }
}
