package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum RoadType implements StringRepresentable {
    NONE("none", 0, 0xFFFFFFFF),
    ASPHALT("asphalt", 1, 0xFF373432),
	CONCRETE("concrete", 2, 0xFFB9B3A7);
	
	private String roadType;
	private int index;
	private int color;
	
	private RoadType(String roadType, int index, int color) {
		this.roadType = roadType;
		this.index = index;
        this.color = color;
	}
	
	public String getRoadType() {
		return this.roadType;
	}

	public int getIndex() {
		return this.index;
	}

    public int getColor() {
        return this.color;
    }

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.road.roadtype.%s", roadType);
	}

	public static RoadType getRoadTypeByIndex(int index) {
		for (RoadType controlType : RoadType.values()) {
			if (controlType.getIndex() == index) {
				return controlType;
			}
		}
		return RoadType.NONE;
	}

    @Override
    public String getSerializedName() {
        return this.roadType;
    }

	public Block getBlock() {
        switch (this) {
            default:
            case ASPHALT:
                return ModBlocks.ASPHALT.get();
            case CONCRETE:
                return ModBlocks.CONCRETE.get();
        }
    }

    public Block getSlope() {
        switch (this) {
            default:
            case ASPHALT:
                return ModBlocks.ASPHALT_SLOPE.get();
            case CONCRETE:
                return ModBlocks.CONCRETE_SLOPE.get();
        }
    }
}
