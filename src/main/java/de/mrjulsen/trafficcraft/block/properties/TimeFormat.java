package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.util.StringRepresentable;

public enum TimeFormat implements StringRepresentable {
    TICKS(0, "ticks"),
    HOURS_24(1, "hours_24"),
    HOURS_12(2, "hours_12");
	
	private String format;
	private int index;
	
	private TimeFormat(int index, String format) {
		this.format = format;
		this.index = index;
	}
	
	public String getFormat() {
		return this.format;
	}

	public int getIndex() {
		return this.index;
	}

	public String getTranslationKey() {
		return String.format("gui.trafficcraft.daytime.time_format.%s", format);
	}

	public static TimeFormat getFormatByIndex(int index) {
		for (TimeFormat shape : TimeFormat.values()) {
			if (shape.getIndex() == index) {
				return shape;
			}
		}
		return TimeFormat.TICKS;
	}

    @Override
    public String getSerializedName() {
        return this.format;
    }
}
