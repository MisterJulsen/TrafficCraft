package de.mrjulsen.trafficcraft.block.colors;

import de.mrjulsen.trafficcraft.util.PaintColor;

public interface IColorStorageBlockEntity {

    void setColor(PaintColor color);
    PaintColor getColor();
}
