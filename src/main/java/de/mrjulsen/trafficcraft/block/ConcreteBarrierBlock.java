package de.mrjulsen.trafficcraft.block;

import java.util.Map;

import de.mrjulsen.trafficcraft.block.properties.ColorableBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class ConcreteBarrierBlock extends ColorableBlock implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    
    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_52346_) -> {
        return p_52346_.getKey().getAxis().isHorizontal();
    }).collect(Util.toMap());

    private static final VoxelShape SHAPE_SIDE_EAST = Shapes.or(
        Block.box(8, 0, 3, 16, 4, 13),
        Block.box(8, 10, 6, 16, 16, 10),
        Block.box(8, 4, 3.5, 16, 7, 12.5),
        Block.box(8, 7, 4.75, 16, 10, 11.25)
    );
    private static final VoxelShape SHAPE_SIDE_WEST = Shapes.or(
        Block.box(0, 0, 3, 8, 4, 13),
        Block.box(0, 10, 6, 8, 16, 10),
        Block.box(0, 4, 3.5, 8, 7, 12.5),
        Block.box(0, 7, 4.75, 8, 10, 11.25)
    );
    private static final VoxelShape SHAPE_SIDE_SOUTH = Shapes.or(
        Block.box(3, 0, 8, 13, 4, 16),
        Block.box(6, 10, 8, 10, 16, 16),
        Block.box(3.5, 4, 8, 12.5, 7, 16),
        Block.box(4.75, 7, 8, 11.25, 10, 16)
    );
    private static final VoxelShape SHAPE_SIDE_NORTH = Shapes.or(
        Block.box(3, 0, 0, 13, 4, 8),
        Block.box(6, 10, 0, 10, 16, 8),
        Block.box(3.5, 4, 0, 12.5, 7, 8),
        Block.box(4.75, 7, 0, 11.25, 10, 8)
    );
    private static final VoxelShape SHAPE_BASE = Shapes.or(
        Block.box(3, 0, 3, 13, 4, 13),
        Block.box(6, 4, 6, 10, 16, 10)
    );


    private static final VoxelShape COLLISION_EAST = Shapes.or(Block.box(8, 0, 3, 16, 4, 13), Block.box(8, 4, 4.75, 16, 24, 11.25));
    private static final VoxelShape COLLISION_WEST = Shapes.or(Block.box(0, 0, 3, 8, 4, 13), Block.box(0, 4, 4.75, 8, 24, 11.25));
    private static final VoxelShape COLLISION_SOUTH = Shapes.or(Block.box(3, 0, 8, 13, 4, 16), Block.box(4.75, 4, 8, 11.25, 24, 16));
    private static final VoxelShape COLLISION_NORTH = Shapes.or(Block.box(3, 0, 0, 13, 4, 8), Block.box(4.75, 4, 0, 11.25, 24, 8));
    private static final VoxelShape COLLISION_BASE = Shapes.or(Block.box(3, 0, 3, 13, 4, 13), Block.box(6, 4, 6, 10, 24, 10));
    
    public ConcreteBarrierBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE)
            .strength(3f) 
            .sound(SoundType.STONE)  
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)  
            .setValue(WATERLOGGED, false)
            .setValue(NORTH, false)      
            .setValue(SOUTH, false)      
            .setValue(WEST, false)      
            .setValue(EAST, false)      
        );
        
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //return pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH ? SHAPE_SN : SHAPE_EW;
        if ((pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH) && !pState.getValue(NORTH) && !pState.getValue(SOUTH)) {
            return Shapes.or(SHAPE_SIDE_EAST, SHAPE_SIDE_WEST);
        } else if ((pState.getValue(FACING) == Direction.EAST || pState.getValue(FACING) == Direction.WEST) && !pState.getValue(EAST) && !pState.getValue(WEST)) {
            return Shapes.or(SHAPE_SIDE_NORTH, SHAPE_SIDE_SOUTH);
        } else {
            VoxelShape shape = SHAPE_BASE;
            if (pState.getValue(NORTH)) {
                shape = Shapes.or(shape, SHAPE_SIDE_NORTH);
            }
            if (pState.getValue(SOUTH)) {
                shape = Shapes.or(shape, SHAPE_SIDE_SOUTH);
            }
            if (pState.getValue(EAST)) {
                shape = Shapes.or(shape, SHAPE_SIDE_EAST);
            }
            if (pState.getValue(WEST)) {
                shape = Shapes.or(shape, SHAPE_SIDE_WEST);
            }
            return shape;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if ((pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH) && !pState.getValue(NORTH) && !pState.getValue(SOUTH)) {
            return Shapes.or(COLLISION_EAST, COLLISION_WEST);
        } else if ((pState.getValue(FACING) == Direction.EAST || pState.getValue(FACING) == Direction.WEST) && !pState.getValue(EAST) && !pState.getValue(WEST)) {
            return Shapes.or(COLLISION_NORTH, COLLISION_SOUTH);
        } else {
            VoxelShape shape = COLLISION_BASE;
            if (pState.getValue(NORTH)) {
                shape = Shapes.or(shape, COLLISION_NORTH);
            }
            if (pState.getValue(SOUTH)) {
                shape = Shapes.or(shape, COLLISION_SOUTH);
            }
            if (pState.getValue(EAST)) {
                shape = Shapes.or(shape, COLLISION_EAST);
            }
            if (pState.getValue(WEST)) {
                shape = Shapes.or(shape, COLLISION_WEST);
            }
            return shape;
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }    

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    public boolean connectsTo(BlockState pState, boolean pIsSideSolid, Direction pDirection) {
        boolean flag = this.isSameBlock(pState);
        return !isExceptionForConnection(pState) && pIsSideSolid || flag;
    }

    private boolean isSameBlock(BlockState pState) {
        return pState.is(this);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockGetter blockgetter = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockState blockstate = blockgetter.getBlockState(blockpos1);
        BlockState blockstate1 = blockgetter.getBlockState(blockpos2);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos3);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos4);
        return super.getStateForPlacement(pContext)
            .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
            .setValue(NORTH, Boolean.valueOf(this.connectsTo(blockstate, blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH), Direction.SOUTH)))
            .setValue(EAST, Boolean.valueOf(this.connectsTo(blockstate1, blockstate1.isFaceSturdy(blockgetter, blockpos2, Direction.WEST), Direction.WEST)))
            .setValue(SOUTH, Boolean.valueOf(this.connectsTo(blockstate2, blockstate2.isFaceSturdy(blockgetter, blockpos3, Direction.NORTH), Direction.NORTH)))
            .setValue(WEST, Boolean.valueOf(this.connectsTo(blockstate3, blockstate3.isFaceSturdy(blockgetter, blockpos4, Direction.EAST), Direction.EAST)))
            .setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
           pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
  
        return pFacing.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(this.connectsTo(pFacingState, pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing.getOpposite()), pFacing.getOpposite()))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(NORTH, EAST, WEST, SOUTH, FACING, WATERLOGGED);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public int getDefaultColor() {
        return 0xFFABABAB;
    }
}
