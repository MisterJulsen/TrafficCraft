package de.mrjulsen.trafficcraft.block.data;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.world.level.ItemLike;

public enum TrafficLightControlType implements ITranslatableEnum, IItemIcon, IIterableEnum<TrafficLightControlType> {
    STATIC("static", 0, ModBlocks.TRAFFIC_LIGHT.get()),
	OWN_SCHEDULE("own_schedule", 1, ModItems.PATTERN_CATALOGUE.get()),
	REMOTE("remote", 2, ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get());
	
	private String controlType;
	private byte index;
	private ItemLike icon;
	
	private TrafficLightControlType(String controlType, int index, ItemLike icon) {
		this.controlType = controlType;
		this.index = (byte)index;
		this.icon = icon;
	}
	
	public String getControlType() {
		return this.controlType;
	}

	public byte getIndex() {
		return this.index;
	}

	@Override
	public ItemLike getItemIcon() {
		return icon;
	}

	public String getValueShortTranslationKey() {
		return String.format("enum.trafficcraft.trafficlightcontroltype.short.%s", controlType);
	}

	public static TrafficLightControlType getControlTypeByIndex(byte index) {
		return Arrays.stream(TrafficLightControlType.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightControlType.STATIC);
	}

    @Override
    public String getSerializedName() {
        return controlType;
    }

	@Override
	public Data getTranslationData() {
		return new Data(TrafficCraft.MOD_ID, "trafficlightcontroltype", controlType);
	}

	@Override
	public TrafficLightControlType[] getValues() {
		return values();
	}
}
