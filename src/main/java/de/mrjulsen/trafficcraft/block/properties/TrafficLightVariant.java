package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightVariant implements StringRepresentable {
    NORMAL("normal", 0),
	SMALL("small", 1),
	SPECIAL("special", 2),
	PEDESTRIAN("pedestrian", 3);
	
	private String variant;
	private int index;
	
	private TrafficLightVariant(String shape, int index) {
		this.variant = shape;
		this.index = index;
	}
	
	public String getVariant() {
		return this.variant;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.variant.%s", variant);
	}

	public static TrafficLightVariant getVariantByIndex(int index) {
		for (TrafficLightVariant shape : TrafficLightVariant.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightVariant.NORMAL;
	}

    @Override
    public String getSerializedName() {
        return variant;
    }
}
