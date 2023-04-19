package de.mrjulsen.trafficcraft.item.properties;

import net.minecraft.world.level.block.Block;

public interface ILinkerItem {
    public boolean isTargetBlockAccepted(Block block);
    public boolean isSourceBlockAccepted(Block block);
}
