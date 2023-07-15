package de.mrjulsen.trafficcraft.block;

import java.util.Map;

import de.mrjulsen.trafficcraft.block.properties.ITrafficPostLike;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class StreetLightBlock extends StreetLampBaseBlock {
    
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;

    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().collect(Util.toMap());

    private static final VoxelShape SHAPE_BASE_SN = Block.box(5, 5.75D, 3, 11, 9.3D, 13);
    private static final VoxelShape SHAPE_BASE_EW = Block.box(3, 5.75D, 5, 13, 9.3D, 11);
    private static final VoxelShape SHAPE_NORTH = Block.box(7, 7, 0, 9, 9, 7);
    private static final VoxelShape SHAPE_EAST = Block.box(9, 7, 7, 16, 9, 9);
    private static final VoxelShape SHAPE_SOUTH = Block.box(7, 7, 9, 9, 9, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 7, 7, 7, 9, 9);
    private static final VoxelShape SHAPE_UP = Block.box(7, 9, 7, 9, 16, 9);
    
    public StreetLightBlock() {
        super(LampType.SINGLE_LIGHT);

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false)
            .setValue(UP, false)
        );
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE_COMMON;
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
    
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        BlockState state = super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);

        return this.canConnect(pState, pFacing) ? state.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(this.connectsTo(pState, pFacingState, pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing.getOpposite()), pFacing.getOpposite()))) : state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {

        BlockGetter blockgetter = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockPos blockpos5 = blockpos.above();
        BlockState blockstate1 = blockgetter.getBlockState(blockpos1);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos2);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos3);
        BlockState blockstate4 = blockgetter.getBlockState(blockpos4);
        BlockState blockstate5 = blockgetter.getBlockState(blockpos5);

        BlockState newState = super.getStateForPlacement(pContext);

        return newState
            .setValue(NORTH, Boolean.valueOf(this.connectsTo(newState, blockstate1, blockstate1.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH), Direction.SOUTH)))
            .setValue(EAST, Boolean.valueOf(this.connectsTo(newState, blockstate2, blockstate2.isFaceSturdy(blockgetter, blockpos2, Direction.WEST), Direction.WEST)))
            .setValue(SOUTH, Boolean.valueOf(this.connectsTo(newState, blockstate3, blockstate3.isFaceSturdy(blockgetter, blockpos3, Direction.NORTH), Direction.NORTH)))
            .setValue(WEST, Boolean.valueOf(this.connectsTo(newState, blockstate4, blockstate4.isFaceSturdy(blockgetter, blockpos4, Direction.EAST), Direction.EAST)))
            .setValue(UP, Boolean.valueOf(this.connectsTo(newState, blockstate5, blockstate5.isFaceSturdy(blockgetter, blockpos3, Direction.UP), Direction.UP)))
        ;
    }

    private boolean isSameBlock(BlockState pState) {
        return pState.is(this);
    }

    public boolean connectsTo(BlockState pState, BlockState pTargetState, boolean pIsSideSolid, Direction pDirection) {
        boolean flag = this.isSameBlock(pTargetState);
        boolean canConnect = false;

        if (pTargetState.getBlock() instanceof ITrafficPostLike postLike) {
            canConnect = postLike.canConnect(pTargetState, pDirection);
        }
        return !isExceptionForConnection(pTargetState) && (canConnect || (pState.getValue(FACING) == pDirection && pIsSideSolid)) || flag;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(NORTH, SOUTH, WEST, EAST, UP);
    }
    
    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return false;
    }

    @Override
    public boolean canConnect(BlockState pState, Direction pDirection) {
        return pDirection != Direction.DOWN;
    }
}
