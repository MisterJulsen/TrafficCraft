package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.data.IIdentifiable;
import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficSignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<TrafficSignShape> SHAPE = EnumProperty.create("shape", TrafficSignShape.class);

    public TrafficSignBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(1.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .sound(SoundType.LANTERN)
        );

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(WATERLOGGED, false)
            .setValue(SHAPE, TrafficSignShape.SQUARE)
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(SHAPE).getVoxelShape(pState.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (item instanceof PatternCatalogueItem patternItem && ((item instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack)) || PatternCatalogueItem.getSelectedPattern(stack) != null)) {
            if (pLevel.getBlockEntity(pPos) instanceof TrafficSignBlockEntity blockEntity) {
                if (item instanceof CreativePatternCatalogueItem creativeItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack)) {
                    blockEntity.setAndResetTexture(CreativePatternCatalogueItem.getCustomImage(stack).getTexture());
                } else {
                    blockEntity.setAndResetTexture(PatternCatalogueItem.getSelectedPattern(stack).getTexture());
                }
            }
            
            if (pLevel.isClientSide) {
                pLevel.playSound(pPlayer, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.3F, 1.5f);
            }
            pLevel.setBlockAndUpdate(pPos, pState.setValue(SHAPE, item instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack) ? CreativePatternCatalogueItem.getCustomImage(stack).getShape() : PatternCatalogueItem.getSelectedPattern(stack).getShape()));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, WATERLOGGED, SHAPE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
           pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
  
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return pState.getValue(SHAPE) != TrafficSignShape.SMALL_LOWER && pState.getValue(FACING).getOpposite() == pDirection;
    }

    /* BLOCK ENTITY */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrafficSignBlockEntity(pPos, pState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.isClientSide) {
            if (pLevel.getBlockEntity(pPos) instanceof IIdentifiable id) {
                ClientWrapper.clearTexture(id);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
