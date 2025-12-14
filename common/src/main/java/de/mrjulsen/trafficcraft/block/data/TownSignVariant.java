package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.trafficcraft.TrafficCraft;

public enum TownSignVariant implements ITranslatableEnum {
    FRONT("front", 0),
	BACK("back", 1),
	BOTH("both", 2);
	
	private String variant;
	private int index;
	
	private TownSignVariant(String variant, int index) {
		this.variant = variant;
		this.index = index;
	}
	
	public String getVariant() {
		return this.variant;
	}

	public int getIndex() {
		return this.index;
	}

	public static TownSignVariant getVariantByIndex(int index) {
		for (TownSignVariant shape : TownSignVariant.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TownSignVariant.FRONT;
	}

    @Override
    public String getSerializedName() {
        return variant;
    }

	@Override
	public Data getTranslationData() {
		return new Data(TrafficCraft.MOD_ID, "townsignvariant", variant);
	}
}
