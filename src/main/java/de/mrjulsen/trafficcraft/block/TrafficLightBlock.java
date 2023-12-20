package de.mrjulsen.trafficcraft.block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.data.ColorableBlock;
import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrafficLightBlock extends ColorableBlock implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<TrafficLightModel> MODEL = EnumProperty.create("model", TrafficLightModel.class);

    @Deprecated public static final EnumProperty<de.mrjulsen.trafficcraft.block.data.compat.TrafficLightVariant> VARIANT = EnumProperty.create("variant", de.mrjulsen.trafficcraft.block.data.compat.TrafficLightVariant.class);
    @Deprecated public static final EnumProperty<de.mrjulsen.trafficcraft.block.data.compat.TrafficLightDirection> DIRECTION = EnumProperty.create("direction", de.mrjulsen.trafficcraft.block.data.compat.TrafficLightDirection.class);
    @Deprecated public static final EnumProperty<de.mrjulsen.trafficcraft.block.data.compat.TrafficLightMode> MODE = EnumProperty.create("mode", de.mrjulsen.trafficcraft.block.data.compat.TrafficLightMode.class);

    public static final VoxelShape SHAPE_COMMON = Block.box(7, 0, 7, 9, 16, 9);

    private static final Map<TrafficLightModel, Map<Direction, VoxelShape>> shapes = new HashMap<>();
    static {
        Arrays.stream(TrafficLightModel.values()).forEach(x -> {
            Map<Direction, VoxelShape> voxelShapes = new HashMap<>();
            voxelShapes.put(Direction.NORTH, Block.box(4, x.getHitboxBottom(), 1, 12, x.getHitboxTop(), 6));
            voxelShapes.put(Direction.SOUTH, Block.box(4, x.getHitboxBottom(), 10, 12, x.getHitboxTop(), 15));
            voxelShapes.put(Direction.EAST, Block.box(10, x.getHitboxBottom(), 4, 15, x.getHitboxTop(), 12));
            voxelShapes.put(Direction.WEST, Block.box(1, x.getHitboxBottom(), 4, 6, x.getHitboxTop(), 12));        
            shapes.put(x, voxelShapes);
        });
    }

    public TrafficLightBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(5f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.ANVIL)
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(MODEL, TrafficLightModel.THREE_LIGHTS)
            //.setValue(VARIANT, TrafficLightVariant.NORMAL)
            //.setValue(DIRECTION, TrafficLightDirection.NORMAL)
            //.setValue(MODE, TrafficLightMode.OFF)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(MODEL, TrafficLightModel.THREE_LIGHTS));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {

        // TODO: Don't know if this works...
        
        if (level.getBlockEntity(pos) instanceof TrafficLightBlockEntity blockEntity) {
            boolean isPedestrian = false;
            if (oldState.getOptionalValue(MODE).isPresent()) {
                blockEntity.enableOnlyColors(oldState.getValue(MODE).convertToColorList());
            }
            if (oldState.getOptionalValue(VARIANT).isPresent()) {
                newState = newState.setValue(MODEL, oldState.getValue(VARIANT).convertToModel());
                isPedestrian = oldState.getValue(VARIANT) == de.mrjulsen.trafficcraft.block.data.compat.TrafficLightVariant.PEDESTRIAN;
            }
            if (oldState.getOptionalValue(DIRECTION).isPresent()) {
                blockEntity.setIcon(oldState.getValue(DIRECTION).convertToIcon(isPedestrian));
            }
        }
        
        super.onBlockStateChange(level, pos, oldState, newState);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE_COMMON;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.or(SHAPE_COMMON, shapes.get(pState.getValue(MODEL)).get((Direction)pState.getValue(FACING)));
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

        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(flag)).setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATERLOGGED, FACING, MODEL);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }   
    
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (item instanceof BrushItem) {
            return InteractionResult.FAIL;
        }

        if (pLevel.isClientSide && item instanceof WrenchItem) {
            if (!pPlayer.isShiftKeyDown())
                ClientWrapper.showTrafficLightConfigScreen(pLevel, pPos);
                
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
        
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof TrafficLightBlockEntity blockEntity) {
            if (pLevel.hasNeighborSignal(pPos)) {
                if (blockEntity.getSchedule().getTrigger() == TrafficLightTrigger.REDSTONE && !blockEntity.isPowered()) {
                    blockEntity.setPowered(true);
                    blockEntity.startSchedule(true);
                }
            } else {
                blockEntity.setPowered(false);
                if (blockEntity.getSchedule().getTrigger() == TrafficLightTrigger.REDSTONE) {
                    blockEntity.stopSchedule();
                }
            }                    
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof TrafficLightBlockEntity blockEntity && blockEntity.isRunning() ? 15 : 0;
    }

    /* BLOCK ENTITY */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrafficLightBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), TrafficLightBlockEntity::tick);
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return pDirection != pState.getValue(FACING);
    }
}
