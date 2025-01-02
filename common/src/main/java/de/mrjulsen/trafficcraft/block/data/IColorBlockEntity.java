package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.trafficcraft.data.PaintColor;

public interface IColorBlockEntity {

    public static final String NBT_COLOR = "color";

    void setColor(PaintColor color);
    PaintColor getColor();
}
