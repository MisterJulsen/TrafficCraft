package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightTrigger implements StringRepresentable, ITranslatableEnum {
    NONE("none", 0),
	ON_REQUEST("on_request", 1),
	REDSTONE("redstone", 2);
	
	private String trigger;
	private byte index;
	
	private TrafficLightTrigger(String shape, int index) {
		this.trigger = shape;
		this.index = (byte)index;
	}
	
	public String getTrigger() {
		return this.trigger;
	}

	public byte getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.trigger.%s", trigger);
	}

	public static TrafficLightTrigger getTriggerByIndex(byte index) {
		return Arrays.stream(TrafficLightTrigger.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightTrigger.NONE);
	}

    @Override
    public String getSerializedName() {
        return trigger;
    }

	@Override
	public String getEnumName() {
		return "trafficlighttrigger";
	}

	@Override
	public String getEnumValueName() {
		return getTrigger();
	}
}
