package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightIcon implements StringRepresentable, ITranslatableEnum {
    NONE("none", 0),
	RIGHT("right", 1),
	LEFT("left", 2),
	STRAIGHT("straight", 3),
	STRAIGHT_RIGHT("straight_right", 4),
	STRAIGHT_LEFT("straight_left", 5),
	PEDESTRIAN("pedestrian", 6);
	
	private String name;
	private int index;
	
	private TrafficLightIcon(String name, int index) {
		this.name = name;
		this.index = index;
	}
	
	public String getName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlighticon.%s", name);
	}

	public static TrafficLightIcon getIconByIndex(int index) {
		for (TrafficLightIcon shape : TrafficLightIcon.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightIcon.NONE;
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
