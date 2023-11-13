package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.trafficcraft.data.PaintColor;

public interface IColorBlockEntity {

    void setColor(PaintColor color);
    PaintColor getColor();
}
