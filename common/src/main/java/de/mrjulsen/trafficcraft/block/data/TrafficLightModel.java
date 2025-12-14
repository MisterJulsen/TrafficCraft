package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;

public enum TrafficLightModel implements ITranslatableEnum {
    ONE_LIGHT("single", () -> ModGuiIcons.TRAFFIC_LIGHT_1_LIGHT.getAsSprite(16, 16), 1, 9, 16),
	TWO_LIGHTS("double", () -> ModGuiIcons.TRAFFIC_LIGHT_2_LIGHTS.getAsSprite(16, 16), 2, 4.5f, 16),
	THREE_LIGHTS("tripple", () -> ModGuiIcons.TRAFFIC_LIGHT_3_LIGHTS.getAsSprite(16, 16), 3, -0.5f, 16);
	
	private String name;
	private Supplier<DLSprite> icon;
	private byte lightsCount;
	private float hitboxBottom;
	private float hitboxTop;
	
	private TrafficLightModel(String name, Supplier<DLSprite> icon, int lightsCount, float hitboxBottom, float hitboxTop) {
		this.name = name;
		this.icon = icon;
		this.lightsCount = (byte)lightsCount;
		this.hitboxBottom = hitboxBottom;
		this.hitboxTop = hitboxTop;
	}
	
	public String getName() {
		return this.name;
	}

	public DLSprite getIcon() {
		return icon.get();
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

	public static TrafficLightModel getModelByLightsCount(byte lightsCount) {
		return Arrays.stream(TrafficLightModel.values()).filter(x -> x.getLightsCount() == lightsCount).findFirst().orElse(TrafficLightModel.THREE_LIGHTS);
	}

    @Override
    public String getSerializedName() {
        return name;
    }

	@Override
	public Data getTranslationData() {
		return new Data(TrafficCraft.MOD_ID, "trafficlightmodel", name);
	}

    public static byte maxRequiredSlots() {
        return (byte)Arrays.stream(TrafficLightModel.values()).mapToInt(x -> x.getLightsCount()).max().getAsInt();
    }
}
