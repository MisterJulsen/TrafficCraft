package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum TrafficSignShape implements StringRepresentable {
    CIRCLE("circle", 0),
	SQUARE("square", 1),
	TRIANGLE("triangle", 2),
	DIAMOND("diamond", 3),
	RECTANGLE("rectangle", 4),	
	MISC("misc", 5);
	
	private String shape;
	private int index;
	
	private TrafficSignShape(String shape, int index) {
		this.shape = shape;
		this.index = index;
	}
	
	public String getShape() {
		return this.shape;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.signpicker.tab.%s", shape);
	}

	public static TrafficSignShape getShapeByIndex(int index) {
		for (TrafficSignShape shape : TrafficSignShape.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TrafficSignShape.CIRCLE;
	}

	public final ResourceLocation getIconResourceLocation() {
		return new ResourceLocation(ModMain.MOD_ID, String.format("textures/block/sign/icons/%s.png", this.getShape()));
	}

    @Override
    public String getSerializedName() {
        return shape;
    }
}
