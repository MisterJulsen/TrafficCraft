package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.properties.ColorableBlock;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TrafficConeBlock extends ColorableBlock implements SimpleWaterloggedBlock, Wearable {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(7, 13, 7, 9, 16, 9),
        Block.box(2, 0, 2, 14, 1, 14),
        Block.box(3, 1, 3, 13, 4, 13),
        Block.box(4, 4, 4, 12, 7, 12),
        Block.box(5, 7, 5, 11, 10, 11),
        Block.box(6, 10, 6, 10, 13, 10)
    );

    private static final VoxelShape COLLISION = Block.box(3, 0, 3, 13, 24, 13);
    
    public TrafficConeBlock() {
        super(BlockBehaviour.Properties.of(Material.BAMBOO)
            .strength(0.2f)
            .noOcclusion()
            .sound(SoundType.BAMBOO)
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(WATERLOGGED, false)        
        );
    }    

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return COLLISION;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;

        return this.defaultBlockState()
            .setValue(WATERLOGGED, Boolean.valueOf(flag))
        ;
    }

    @Override
    public boolean collisionExtendsVertically(BlockState state, BlockGetter level, BlockPos pos, Entity collidingEntity) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
           pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
  
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATERLOGGED);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public int getDefaultColor() {
        return Constants.TRAFFIC_CONE_BASE_COLOR;
    }
}
