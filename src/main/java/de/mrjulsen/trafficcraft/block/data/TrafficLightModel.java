package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightModel implements StringRepresentable, ITranslatableEnum, IIconEnum {
    ONE_LIGHT("single", 1, 9, 16, 2, 0),
	TWO_LIGHTS("double", 2, 4.5f, 16, 3, 0),
	THREE_LIGHTS("tripple", 3, -0.5f, 16, 4, 0);
	
	private String name;
	private byte lightsCount;
	private float hitboxBottom;
	private float hitboxTop;
	private int uMul;
	private int vMul;
	
	private TrafficLightModel(String name, int lightsCount, float hitboxBottom, float hitboxTop, int u, int v) {
		this.name = name;
		this.lightsCount = (byte)lightsCount;
		this.hitboxBottom = hitboxBottom;
		this.hitboxTop = hitboxTop;
		this.uMul = u;
		this.vMul = v;
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

	public float getTotalHitboxHeight() {
		return Math.abs(getHitboxTop() - getHitboxBottom());
	}

	@Override
	public int getUMultiplier() {
		return uMul;
	}

	@Override
	public int getVMultiplier() {
		return vMul;
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
