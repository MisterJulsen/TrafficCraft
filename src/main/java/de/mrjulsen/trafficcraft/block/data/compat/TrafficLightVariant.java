package de.mrjulsen.trafficcraft.block.data.compat;

import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import net.minecraft.util.StringRepresentable;

/**
 * @deprecated Only for compatibility purposes! 
 */
@Deprecated
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
	
	public TrafficLightModel convertToModel() {
		switch (this) {
			case SMALL:
			case SPECIAL:
			case PEDESTRIAN:
				return TrafficLightModel.TWO_LIGHTS;
			case NORMAL:
			default:
				return TrafficLightModel.THREE_LIGHTS;
		}
	}
}
