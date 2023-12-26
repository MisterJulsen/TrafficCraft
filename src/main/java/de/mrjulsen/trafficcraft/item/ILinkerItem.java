package de.mrjulsen.trafficcraft.item;

import net.minecraft.world.level.block.Block;

public interface ILinkerItem {
    public boolean isTargetBlockAccepted(Block block);
    public boolean isSourceBlockAccepted(Block block);
    public String getNameForValidSourceBlock(Block block);
}
