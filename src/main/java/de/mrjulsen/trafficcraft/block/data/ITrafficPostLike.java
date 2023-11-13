package de.mrjulsen.trafficcraft.block.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface ITrafficPostLike {

    boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection);

    default boolean canConnect(BlockState pState, Direction pDirection) {
        return pDirection == Direction.UP || pDirection == Direction.DOWN;
    }
}
