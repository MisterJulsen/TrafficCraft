package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.trafficcraft.TrafficCraft;

public enum TrafficLightType implements ITranslatableEnum, IIconEnum {
    CAR("car", 0, 0, 0),
	TRAM("tram", 1, 1, 0);
	
	private String name;
	private byte index;
	private int uMul;
	private int vMul;
	
	private TrafficLightType(String name, int index, int u, int v) {
		this.name = name;
		this.index = (byte)index;
		this.uMul = u;
		this.vMul = v;
	}
	
	public String getName() {
		return this.name;
	}

	public byte getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("enum.trafficcraft.trafficlighttype.%s", name);
	}

	@Override
	public int getUMultiplier() {
		return uMul;
	}

	@Override
	public int getVMultiplier() {
		return vMul;
	}

	public static TrafficLightType getTypeByIndex(byte index) {
		return Arrays.stream(TrafficLightType.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightType.CAR);
	}

	@Override
	public Data getTranslationData() {
		return new Data(TrafficCraft.MOD_ID, "trafficlighttype", name);
	}
}
