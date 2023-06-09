package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.util.StringRepresentable;

public enum TrafficLightTrigger implements StringRepresentable {
    NONE("none", 0),
	ON_REQUEST("on_request", 1),
	REDSTONE("redstone", 2);
	
	private String trigger;
	private int index;
	
	private TrafficLightTrigger(String shape, int index) {
		this.trigger = shape;
		this.index = index;
	}
	
	public String getTrigger() {
		return this.trigger;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.trigger.%s", trigger);
	}

	public static TrafficLightTrigger getTriggerByIndex(int index) {
		for (TrafficLightTrigger shape : TrafficLightTrigger.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficLightTrigger.NONE;
	}

    @Override
    public String getSerializedName() {
        return trigger;
    }
}
