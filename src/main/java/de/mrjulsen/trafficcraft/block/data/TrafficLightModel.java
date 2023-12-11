package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public enum TrafficLightModel implements StringRepresentable, ITranslatableEnum {
    ONE_LIGHT("single", 1),
	TWO_LIGHTS("double", 2),
	THREE_LIGHTS("tripple", 3);
	
	private String name;
	private int lightsCount;
	
	private TrafficLightModel(String name, int lightsCount) {
		this.name = name;
		this.lightsCount = lightsCount;
	}
	
	public String getName() {
		return this.name;
	}

	public int getLightsCount() {
		return this.lightsCount;
	}

	public static TrafficLightModel getModelByLightsCount(int lightsCount) {
		for (TrafficLightModel shape : TrafficLightModel.values()) {
			if (shape.getLightsCount() == lightsCount) {
				return shape;
			}
		}
		return TrafficLightModel.THREE_LIGHTS;
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
}
