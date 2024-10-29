package de.mrjulsen.trafficcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoadBarrierFenceBlock extends TrafficConeBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
        Block.box(5, 0, 3, 11, 2, 13),
        Block.box(7, 2, 7, 9, 16, 9),
        Block.box(0, 12, 6, 16, 16, 7),
        Block.box(0, 5, 6, 16, 9, 7)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
        Block.box(5, 0, 3, 11, 2, 13),
        Block.box(7, 2, 7, 9, 16, 9),
        Block.box(0, 12, 9, 16, 16, 10),
        Block.box(0, 5, 9, 16, 9, 10)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
        Block.box(3, 0, 5, 13, 2, 11),
        Block.box(7, 2, 7, 9, 16, 9),
        Block.box(6, 12, 0, 7, 16, 16),
        Block.box(6, 5, 0, 7, 9, 16)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
        Block.box(3, 0, 5, 13, 2, 11),
        Block.box(7, 2, 7, 9, 16, 9),
        Block.box(9, 12, 0, 10, 16, 16),
        Block.box(9, 5, 0, 10, 9, 16)
    );

    private static final VoxelShape COLLISION_NORTH = Shapes.or(
        Block.box(5, 0, 3, 11, 2, 13),
        Block.box(7, 2, 7, 9, 24, 9),
        Block.box(0, 5, 6, 16, 24, 7)
    );
    private static final VoxelShape COLLISION_SOUTH = Shapes.or(
        Block.box(5, 0, 3, 11, 2, 13),
        Block.box(7, 2, 7, 9, 24, 9),
        Block.box(0, 5, 9, 16, 24, 10)
    );
    private static final VoxelShape COLLISION_WEST = Shapes.or(
        Block.box(3, 0, 5, 13, 2, 11),
        Block.box(7, 2, 7, 9, 24, 9),
        Block.box(6, 5, 0, 7, 24, 16)
    );
    private static final VoxelShape COLLISION_EAST = Shapes.or(
        Block.box(3, 0, 5, 13, 2, 11),
        Block.box(7, 2, 7, 9, 24, 9),
        Block.box(9, 5, 0, 10, 24, 16)
    );
    
    public RoadBarrierFenceBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)  
            .setValue(WATERLOGGED, false)        
        );
    }
    
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
            default:
            case NORTH:
                return SHAPE_NORTH;
            case EAST:
                return SHAPE_EAST;
            case SOUTH:
                return SHAPE_SOUTH;
            case WEST:
                return SHAPE_WEST;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
            default:
            case NORTH:
                return COLLISION_NORTH;
            case EAST:
                return COLLISION_EAST;
            case SOUTH:
                return COLLISION_SOUTH;
            case WEST:
                return COLLISION_WEST;
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }    

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {

        return super.getStateForPlacement(pContext)
            .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
        ;
    }
}
