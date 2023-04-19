package de.mrjulsen.trafficcraft.block;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AsphaltSlope extends RoadBlock implements SimpleWaterloggedBlock {

    private Block pickupBlock;
    public static final int MAX_HEIGHT = 8;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[] { Shapes.empty(),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D) };
    public static final int HEIGHT_IMPASSABLE = 5;

    public AsphaltSlope(RoadType type, Block pickupBlock) {
        super(Properties.of(Material.STONE)
                .strength(1.5f)
                .requiresCorrectToolForDrops(), type);

        this.pickupBlock = pickupBlock;
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1).setValue(WATERLOGGED, false));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return pickupBlock == null || pickupBlock == this ? super.getCloneItemStack(state, target, level, pos, player) : this.pickupBlock.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        onRemoveColor(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        switch (pType) {
            case LAND:
                return pState.getValue(LAYERS) < 5;
            case WATER:
                return false;
            case AIR:
                return false;
            default:
                return false;
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
            CollisionContext pContext) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {        
        int i = pState.getValue(LAYERS);
        if (pUseContext.getItemInHand().getItem() instanceof BlockItem blockitem && i < MAX_HEIGHT) {
            if (blockitem.getBlock() instanceof AsphaltSlope selectedSlope && pState.getBlock() instanceof AsphaltSlope targetSlope && selectedSlope.getDefaultRoadType() == targetSlope.getDefaultRoadType()) {
                if (pUseContext.replacingClickedOnBlock()) {
                    return pUseContext.getClickedFace() == Direction.UP;
                } else {
                    return true;
                }
            }
        }
        return false;        
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;

        if (blockstate.getBlock() instanceof AsphaltSlope) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(MAX_HEIGHT, i + 1))).setValue(WATERLOGGED, Boolean.valueOf(flag));
        } else {
            return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LAYERS, WATERLOGGED);
        super.createBlockStateDefinition(pBuilder);
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
    public void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (!(item instanceof BrushItem)) {
            return;
        }

        if (this == ModBlocks.ASPHALT_SLOPE.get() || this == ModBlocks.CONCRETE_SLOPE.get()) {
            return;
        }

        switch (this.getDefaultRoadType()) {
            case ASPHALT:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.ASPHALT_SLOPE.get().defaultBlockState()
                    .setValue(RoadBlock.FACING, pState.getValue(RoadBlock.FACING))
                    .setValue(AsphaltSlope.LAYERS, pState.getValue(AsphaltSlope.LAYERS))
                );
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            case CONCRETE:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.CONCRETE_SLOPE.get().defaultBlockState()
                    .setValue(RoadBlock.FACING, pState.getValue(RoadBlock.FACING))
                    .setValue(AsphaltSlope.LAYERS, pState.getValue(AsphaltSlope.LAYERS))
                );
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            default:
                break;
        }
    }
}
