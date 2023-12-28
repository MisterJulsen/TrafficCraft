package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.block.data.ColorableBlock;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaintBucketBlock extends ColorableBlock implements SimpleWaterloggedBlock {
    
    public static final int MAX_PAINT = 8;

    public static final IntegerProperty PAINT = IntegerProperty.create("paint", 0, MAX_PAINT);
    public static final DirectionProperty DIRECTION = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape PAINT_BUCKET = Block.box(4, 0, 4, 12, 10, 12);

    public PaintBucketBlock()
    {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(2f)
            .noOcclusion()
            .sound(SoundType.LANTERN)  
        );
        this.registerDefaultState(this.defaultBlockState()
            .setValue(PAINT, 0)
            .setValue(DIRECTION, Direction.NORTH)
            .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return PAINT_BUCKET;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(PAINT, DIRECTION, WATERLOGGED);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return canSupportCenter(pLevel, pPos.below(), Direction.UP);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(DIRECTION, pRotation.rotate(pState.getValue(DIRECTION)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(DIRECTION)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        FluidState fluidState = pContext.getLevel().getFluidState(pos);

        return this.defaultBlockState()
            .setValue(PAINT, 0)
            .setValue(DIRECTION, pContext.getHorizontalDirection())
            .setValue(WATERLOGGED, Boolean.valueOf(Boolean.valueOf(fluidState.getType() == Fluids.WATER))
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if (state.getValue(WATERLOGGED)) {
            player.displayClientMessage(Utils.translate("block.trafficcraft.paint_bucket.message.underwater").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!(level.getBlockEntity(pos) instanceof ColoredBlockEntity)) {
            return InteractionResult.FAIL;
        }

        IColorBlockEntity blockEntity = (IColorBlockEntity)level.getBlockEntity(pos);

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof BrushItem item) {
            int paint = state.getValue(PAINT);

            // Check if bucket is empty
            if (paint <= 0) {
                if(!level.isClientSide)
                    player.displayClientMessage(Utils.translate("block.trafficcraft.paint_bucket.message.empty").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            // Generate tags if item has no nbt
            if (!stack.hasTag())
                stack.setTag(BrushItem.checkNbt(stack));

            // Set brush color
            if ((stack.getTag().getInt("paint") < item.getMaxPaint() && paint > 0) || (stack.getTag().getInt("paint") == item.getMaxPaint() && stack.getTag().getInt("color") != blockEntity.getColor().getId())) {
                
                if(!level.isClientSide) {
                    
                    if(!player.isCreative())
                        level.setBlockAndUpdate(pos, state.setValue(PAINT, state.getValue(PAINT) - 1));

                    stack.getTag().putInt("paint", item.getMaxPaint());
                    stack.getTag().putInt("color", blockEntity.getColor().getId());
                    level.playSound(player, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        } else if (stack.getItem() instanceof DyeItem dye && !level.isClientSide) { 

            if (state.getValue(PAINT) > 0) {
                if (!blockEntity.getColor().equals(dye.getDyeColor())) {
                    player.displayClientMessage(Utils.translate("block.trafficcraft.paint_bucket.message.wrong_color").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
            }

            if (state.getValue(PAINT) >= MAX_PAINT) {
                player.displayClientMessage(Utils.translate("block.trafficcraft.paint_bucket.message.full").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }

            if (state.getValue(PAINT) < MAX_PAINT) {
                level.setBlockAndUpdate(pos, state.setValue(PAINT, state.getValue(PAINT) + 1));
                blockEntity.setColor(PaintColor.getFromDye(dye.getDyeColor()));
                
                if(!player.isCreative())
                    player.getItemInHand(hand).shrink(1);
                
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, .8F, 0.9F);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
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

    @Override
    public void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {}

    @Override
    public InteractionResult onSetColor(UseOnContext pContext) {
        return InteractionResult.SUCCESS;
    }
}
