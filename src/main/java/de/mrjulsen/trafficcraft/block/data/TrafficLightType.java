package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightType implements StringRepresentable, ITranslatableEnum {
    CAR("car", 0),
	TRAM("tram", 1);
	
	private String name;
	private byte index;
	
	private TrafficLightType(String name, int index) {
		this.name = name;
		this.index = (byte)index;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.trafficlighttype.%s", name);
	}

	public static TrafficLightType getTypeByIndex(byte index) {
		return Arrays.stream(TrafficLightType.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightType.CAR);
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getEnumName() {
		return "trafficlighttype";
	}

	@Override
	public String getEnumValueName() {
		return getName();
	}
}
