package de.mrjulsen.trafficcraft.block.data;

import com.mojang.serialization.Codec;

import de.mrjulsen.trafficcraft.registry.ModBlocks;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

public enum RoadType implements StringRepresentable {
    NONE("none", 0, 0xFFFFFFFF, ModBlocks.ASPHALT),
    ASPHALT("asphalt", 1, 0xFF373432, ModBlocks.ASPHALT),
	CONCRETE("concrete", 2, 0xFFB9B3A7, ModBlocks.CONCRETE);
	
	private String roadType;
	private int index;
	private int color;
    private RegistrySupplier<Block> pickupBlock;

    public static final Codec<RoadType> CODEC = ExtraCodecs.idResolverCodec(RoadType::getIndex, RoadType::getRoadTypeByIndex, 0);
	
	private RoadType(String roadType, int index, int color, RegistrySupplier<Block> pickupBlock) {
		this.roadType = roadType;
		this.index = index;
        this.color = color;
        this.pickupBlock = pickupBlock;
	}
	
	public String getRoadType() {
		return this.roadType;
	}

    public RegistrySupplier<Block> getPickupBlock() {
        return pickupBlock;
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
