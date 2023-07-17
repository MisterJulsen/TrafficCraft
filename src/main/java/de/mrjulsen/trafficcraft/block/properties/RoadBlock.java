package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltBlock;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltSlope;
import de.mrjulsen.trafficcraft.block.colors.IColorStorageBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class RoadBlock extends ColorableBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private RoadType defaultRoadType;

    public RoadBlock(Properties properties, RoadType type) {
        super(properties);
        this.defaultRoadType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
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
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public InteractionResult onSetColor(UseOnContext pContext) {
        String id = "";
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();
        
        if (level.getBlockEntity(pos) instanceof IColorStorageBlockEntity blockEntity) {
            if (state.getBlock() instanceof PaintedAsphaltBlock)
                id = this.getDefaultRoadType().getRoadType() + "_pattern_" + BrushItem.getPatternId(stack);
            else if (state.getBlock() instanceof PaintedAsphaltSlope)
                id = this.getDefaultRoadType().getRoadType() + "_slope_pattern_" + BrushItem.getPatternId(stack);

            if (!ModBlocks.ROAD_BLOCKS.containsKey(id)) {
                return InteractionResult.FAIL;
            }

            BlockState newState = ModBlocks.ROAD_BLOCKS.get(id).get().defaultBlockState().setValue(RoadBlock.FACING, player.getDirection());
                if (state.getBlock() instanceof PaintedAsphaltSlope) {
                    newState = newState.setValue(PaintedAsphaltSlope.LAYERS, state.getValue(PaintedAsphaltSlope.LAYERS));
                }

                level.setBlockAndUpdate(pos, newState);
                super.onSetColor(pContext);

                return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean update(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);

        level.setBlockAndUpdate(pos, state.setValue(RoadBlock.FACING, state.getValue(RoadBlock.FACING).getClockWise(Axis.Y)));
        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);

        return true;
    }
}
