package de.mrjulsen.trafficcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficBarrelBlock extends TrafficConeBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(3, 0, 3, 13, 1, 13),
        Block.box(5, 1, 5, 11, 14, 11),
        Block.box(5, 14, 7, 11, 16, 9)
    );

    private static final VoxelShape COLLISION = Block.box(5, 0, 5, 11, 24, 11);
    
    public TrafficBarrelBlock() {
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
