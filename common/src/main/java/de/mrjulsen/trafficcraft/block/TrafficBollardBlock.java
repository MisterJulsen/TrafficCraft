package de.mrjulsen.trafficcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficBollardBlock extends TrafficConeBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(4, 0, 4, 12, 1, 12),
        Block.box(6, 1, 6, 10, 2, 10),
        Block.box(7, 2, 7, 9, 16, 9)
    );

    private static final VoxelShape COLLISION = Block.box(6, 0, 6, 10, 24, 10);
    
    public TrafficBollardBlock() {
        super();
    }    

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return COLLISION;
    }
}
