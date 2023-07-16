package de.mrjulsen.trafficcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluorescentTubeLampBlock extends StreetLightBlock {

    private static final VoxelShape SHAPE_BASE_SN = Block.box(6, 5.75D, 0, 10, 9.3D, 16);
    private static final VoxelShape SHAPE_BASE_EW = Block.box(0, 5.75D, 6, 16, 9.3D, 10);
    
    public FluorescentTubeLampBlock() {
        super();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH ? SHAPE_BASE_SN : SHAPE_BASE_EW;

        if (Boolean.TRUE.equals(pState.getValue(NORTH))) {
            shape = Shapes.or(shape, SHAPE_NORTH);
        }

        if (Boolean.TRUE.equals(pState.getValue(EAST))) {
            shape = Shapes.or(shape, SHAPE_EAST);
        }

        if (Boolean.TRUE.equals(pState.getValue(SOUTH))) {
            shape = Shapes.or(shape, SHAPE_SOUTH);
        }

        if (Boolean.TRUE.equals(pState.getValue(WEST))) {
            shape = Shapes.or(shape, SHAPE_WEST);
        }

        if (Boolean.TRUE.equals(pState.getValue(UP))) {
            shape = Shapes.or(shape, SHAPE_UP);
        }

        return shape;
    }
}
