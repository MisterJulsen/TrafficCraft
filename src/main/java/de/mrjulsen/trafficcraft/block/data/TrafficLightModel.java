package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightModel implements StringRepresentable, ITranslatableEnum {
    ONE_LIGHT("single", 1, 9, 16),
	TWO_LIGHTS("double", 2, 4.5f, 16),
	THREE_LIGHTS("tripple", 3, -0.5f, 16);
	
	private String name;
	private byte lightsCount;
	private float hitboxBottom;
	private float hitboxTop;
	
	private TrafficLightModel(String name, int lightsCount, float hitboxBottom, float hitboxTop) {
		this.name = name;
		this.lightsCount = (byte)lightsCount;
		this.hitboxBottom = hitboxBottom;
		this.hitboxTop = hitboxTop;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getLightsCount() {
		return this.lightsCount;
	}

	public float getHitboxBottom() {
		return hitboxBottom;
	}

	public float getHitboxTop() {
		return hitboxTop;
	}

	public static TrafficLightModel getModelByLightsCount(byte lightsCount) {
		return Arrays.stream(TrafficLightModel.values()).filter(x -> x.getLightsCount() == lightsCount).findFirst().orElse(TrafficLightModel.THREE_LIGHTS);
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public String getEnumName() {
		return "trafficlightmodel";
	}

	@Override
	public String getEnumValueName() {
		return getName();
	}

    public static byte maxRequiredSlots() {
        return (byte)Arrays.stream(TrafficLightModel.values()).mapToInt(x -> x.getLightsCount()).max().getAsInt();
    }
}
