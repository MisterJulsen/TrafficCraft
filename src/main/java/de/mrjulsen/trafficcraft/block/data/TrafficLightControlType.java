package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightControlType implements StringRepresentable, ITranslatableEnum {
    STATIC("static", 0),
	OWN_SCHEDULE("own_schedule", 1),
	REMOTE("remote", 2);
	
	private String controlType;
	private byte index;
	
	private TrafficLightControlType(String controlType, int index) {
		this.controlType = controlType;
		this.index = (byte)index;
	}
	
	public String getControlType() {
		return this.controlType;
	}

	public byte getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlight.controltype.%s", controlType);
	}

	public static TrafficLightControlType getControlTypeByIndex(byte index) {
		return Arrays.stream(TrafficLightControlType.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightControlType.STATIC);
	}

    @Override
    public String getSerializedName() {
        return controlType;
    }

	@Override
	public String getEnumName() {
		return "trafficlightcontroltype";
	}

	@Override
	public String getEnumValueName() {
		return getControlType();
	}
}
