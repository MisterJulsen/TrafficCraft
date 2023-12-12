package de.mrjulsen.trafficcraft.block.data.compat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import net.minecraft.util.StringRepresentable;

/**
 * @deprecated Only for compatibility purposes! 
 */
@Deprecated
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

	public static Collection<TrafficLightColor> convertToColorList(TrafficLightMode mode) {
        switch (mode) {
            case ALL_ON:
                return List.of(TrafficLightColor.GREEN, TrafficLightColor.YELLOW, TrafficLightColor.RED);
            case GREEN:
                return List.of(TrafficLightColor.GREEN);
            case RED:
                return List.of(TrafficLightColor.RED);
            case RED_YELLOW:
                return List.of(TrafficLightColor.YELLOW, TrafficLightColor.RED);
            default:
                return Collections.emptyList();
        }
    }

	public Collection<TrafficLightColor> convertToColorList() {
        return convertToColorList(this);
    }
}
