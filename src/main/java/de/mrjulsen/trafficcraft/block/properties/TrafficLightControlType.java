package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightControlType implements StringRepresentable {
    STATIC("static", 0),
	OWN_SCHEDULE("own_schedule", 1),
	REMOTE("remote", 2);
	
	private String controlType;
	private int index;
	
	private TrafficLightControlType(String controlType, int index) {
		this.controlType = controlType;
		this.index = index;
	}
	
	public String getControlType() {
		return this.controlType;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.controltype.%s", controlType);
	}

	public static TrafficLightControlType getControlTypeByIndex(int index) {
		for (TrafficLightControlType controlType : TrafficLightControlType.values()) {
			if (controlType.getIndex() == index) {
				return controlType;
			}
		}
		return TrafficLightControlType.STATIC;
	}

    @Override
    public String getSerializedName() {
        return controlType;
    }
}
