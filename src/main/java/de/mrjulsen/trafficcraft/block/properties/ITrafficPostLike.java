package de.mrjulsen.trafficcraft.block.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface ITrafficPostLike {
    Direction[] forbiddenDirections(BlockState state, BlockPos pos);
}
