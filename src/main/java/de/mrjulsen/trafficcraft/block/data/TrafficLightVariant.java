package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

@Deprecated
public enum TrafficLightVariant implements StringRepresentable, ITranslatableEnum {
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

	@Override
	public String getEnumName() {
		return "trafficlightvariant";
	}

	@Override
	public String getEnumValueName() {
		return getVariant();
	}
}
