package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightDirection implements StringRepresentable {
    NORMAL("normal", 0),
	RIGHT("right", 1),
	LEFT("left", 2),
	STRAIGHT("straight", 3),
	STRAIGHT_RIGHT("straight_right", 4),
	STRAIGHT_LEFT("straight_left", 5);
	
	private String directionName;
	private int index;
	
	private TrafficLightDirection(String shape, int index) {
		this.directionName = shape;
		this.index = index;
	}
	
	public String getDirectionName() {
		return this.directionName;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.direction.%s", directionName);
	}

	public static TrafficLightDirection getDirectionByIndex(int index) {
		for (TrafficLightDirection shape : TrafficLightDirection.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightDirection.NORMAL;
	}

    @Override
    public String getSerializedName() {
        return directionName;
    }
}
