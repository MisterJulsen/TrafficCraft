package de.mrjulsen.trafficcraft.block;

import java.util.Map;

import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficSignPostBlock extends Block implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final BooleanProperty EXTEND_BOTTOM = BooleanProperty.create("bottom_extension");

    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().collect(Util.toMap());
    
    private static final VoxelShape SHAPE_BASE = Block.box(7, 7, 7, 9, 9, 9);
    private static final VoxelShape SHAPE_NORTH = Block.box(7, 7, 0, 9, 9, 7);
    private static final VoxelShape SHAPE_EAST = Block.box(9, 7, 7, 16, 9, 9);
    private static final VoxelShape SHAPE_SOUTH = Block.box(7, 7, 9, 9, 9, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 7, 7, 7, 9, 9);
    private static final VoxelShape SHAPE_UP = Block.box(7, 9, 7, 9, 16, 9);
    private static final VoxelShape SHAPE_DOWN = Block.box(7, 0, 7, 9, 7, 9);
    private static final VoxelShape SHAPE_EXTEND_DOWN = Block.box(7, -16, 7, 9, 0, 9);

    public TrafficSignPostBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(1.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.LANTERN)
        );
        
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(WATERLOGGED, false)  
            .setValue(AXIS, Axis.Y)
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false)
            .setValue(UP, false)
            .setValue(DOWN, false)
            .setValue(EXTEND_BOTTOM, false) 
        ); 
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        
        VoxelShape shape = SHAPE_BASE;

        if ((pState.getValue(AXIS) == Axis.X) && !pState.getValue(NORTH) && !pState.getValue(SOUTH) && !pState.getValue(UP) && !pState.getValue(DOWN)) {
            shape = Shapes.or(shape, SHAPE_EAST, SHAPE_WEST);
        } else if ((pState.getValue(AXIS) == Axis.Z) && !pState.getValue(EAST) && !pState.getValue(WEST) && !pState.getValue(UP) && !pState.getValue(DOWN)) {
            shape =  Shapes.or(shape, SHAPE_NORTH, SHAPE_SOUTH);
        } else if ((pState.getValue(AXIS) == Axis.Y) && !pState.getValue(EAST) && !pState.getValue(WEST) && !pState.getValue(NORTH) && !pState.getValue(SOUTH)) {
            shape = Shapes.or(shape, SHAPE_UP, SHAPE_DOWN);
        } else {
            if (pState.getValue(NORTH)) {
                shape = Shapes.or(shape, SHAPE_NORTH);
            }

            if (pState.getValue(EAST)) {
                shape = Shapes.or(shape, SHAPE_EAST);
            }

            if (pState.getValue(SOUTH)) {
                shape = Shapes.or(shape, SHAPE_SOUTH);
            }

            if (pState.getValue(WEST)) {
                shape = Shapes.or(shape, SHAPE_WEST);
            }

            if (pState.getValue(UP)) {
                shape = Shapes.or(shape, SHAPE_UP);
            }

            if (pState.getValue(DOWN)) {
                shape = Shapes.or(shape, SHAPE_DOWN);
            }
        }

        if (pState.getValue(EXTEND_BOTTOM)) {
            shape = Shapes.or(shape, SHAPE_EXTEND_DOWN);
        }

        return shape;
    } 

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return rotatePillar(pState, pRot);
    }

    public static BlockState rotatePillar(BlockState pState, Rotation pRotation) {
        switch(pRotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch((Direction.Axis)pState.getValue(AXIS)) {
                    case X:
                        return pState.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return pState.setValue(AXIS, Direction.Axis.X);
                    default:
                        return pState;
                }
            default:
                return pState;
        }        
    }

    private static boolean needsBottomExtension(BlockState pState, BlockState belowBlock) {
        return pState.getValue(AXIS).test(Direction.UP) && (belowBlock.hasProperty(BlockStateProperties.LAYERS) || (belowBlock.getBlock() instanceof SlabBlock slab && belowBlock.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM));
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
           pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
  
        BlockState belowBlock = pLevel.getBlockState(pCurrentPos.below());

        return pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), this.connectsTo(pLevel, pCurrentPos, pState, pFacingState, pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing.getOpposite()), pFacing.getOpposite()))
            .setValue(EXTEND_BOTTOM, needsBottomExtension(pState, belowBlock))
        ;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;

        BlockGetter blockgetter = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockPos blockpos5 = blockpos.above();
        BlockPos blockpos6 = blockpos.below();
        BlockState blockstate1 = blockgetter.getBlockState(blockpos1);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos2);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos3);
        BlockState blockstate4 = blockgetter.getBlockState(blockpos4);
        BlockState blockstate5 = blockgetter.getBlockState(blockpos5);
        BlockState blockstate6 = blockgetter.getBlockState(blockpos6);

        BlockState newState = this.defaultBlockState()
            .setValue(WATERLOGGED, flag)
            .setValue(AXIS, pContext.getClickedFace().getAxis());

        return newState
            .setValue(NORTH, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate1, blockstate1.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH), Direction.SOUTH.getOpposite()))
            .setValue(EAST, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate2, blockstate2.isFaceSturdy(blockgetter, blockpos2, Direction.WEST), Direction.WEST.getOpposite()))
            .setValue(SOUTH, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate3, blockstate3.isFaceSturdy(blockgetter, blockpos3, Direction.NORTH), Direction.NORTH.getOpposite()))
            .setValue(WEST, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate4, blockstate4.isFaceSturdy(blockgetter, blockpos4, Direction.EAST), Direction.EAST.getOpposite()))
            .setValue(UP, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate5, blockstate5.isFaceSturdy(blockgetter, blockpos3, Direction.DOWN), Direction.DOWN))
            .setValue(DOWN, this.connectsTo(pContext.getLevel(), pContext.getClickedPos(), newState, blockstate6, blockstate6.isFaceSturdy(blockgetter, blockpos4, Direction.UP), Direction.UP))
            .setValue(EXTEND_BOTTOM, needsBottomExtension(newState, blockstate6))
        ;
    }

    private boolean isSameBlock(BlockState pState) {
        return pState.is(this);
    }

    public boolean connectsTo(LevelAccessor level, BlockPos pos, BlockState pState, BlockState pTargetState, boolean pIsSideSolid, Direction pDirection) {
        boolean flag = this.isSameBlock(pTargetState);
        boolean canConnect = false;

        if (pTargetState.getBlock() instanceof ITrafficPostLike postLike) {
            canConnect = postLike.canConnect(pTargetState, pDirection);
        }
        
        return !isExceptionForConnection(pTargetState) && (canConnect || (pState.getValue(AXIS).test(pDirection) && pIsSideSolid)) || flag || (pDirection == Direction.UP && needsBottomExtension(pState, pTargetState));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATERLOGGED, AXIS, NORTH, SOUTH, WEST, EAST, UP, DOWN, EXTEND_BOTTOM);
    }
    
    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return ((pState.getValue(AXIS) == Axis.Y) && !pState.getValue(EAST) && !pState.getValue(WEST) && !pState.getValue(NORTH) && !pState.getValue(SOUTH)) || pState.getValue(UP);
    }

    @Override
    public boolean canConnect(BlockState pState, Direction pDirection) {
        return true;
    } 
}
