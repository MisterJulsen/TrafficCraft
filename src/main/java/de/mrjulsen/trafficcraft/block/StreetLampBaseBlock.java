package de.mrjulsen.trafficcraft.block;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.entity.ModBlockEntities;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class StreetLampBaseBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape SHAPE_COMMON = Block.box(6, 0, 6, 10, 11, 10);

    public static final VoxelShape SHAPE_PART_NORTH = Block.box(6, 4, 6, 10, 11, 25);
    public static final VoxelShape SHAPE_PART_SOUTH = Block.box(6, 4, -9, 10, 11, 10);
    public static final VoxelShape SHAPE_PART_EAST = Block.box(6, 4, 6, 25, 11, 10);
    public static final VoxelShape SHAPE_PART_WEST = Block.box(-9, 4, 6, 10, 11, 10);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(SHAPE_COMMON, SHAPE_PART_NORTH);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(SHAPE_COMMON, SHAPE_PART_SOUTH);
    public static final VoxelShape SHAPE_EAST = Shapes.or(SHAPE_COMMON, SHAPE_PART_EAST);
    public static final VoxelShape SHAPE_WEST = Shapes.or(SHAPE_COMMON, SHAPE_PART_WEST);

    public static final VoxelShape SHAPE_SMALL_PART_NORTH = Block.box(6, 11, 6, 10, 16, 17);
    public static final VoxelShape SHAPE_SMALL_PART_SOUTH = Block.box(6, 11, -1, 10, 16, 10);
    public static final VoxelShape SHAPE_SMALL_PART_EAST = Block.box(6, 11, 6, 17, 16, 10);
    public static final VoxelShape SHAPE_SMALL_PART_WEST = Block.box(-1, 11, 6, 10, 16, 10);
    public static final VoxelShape SHAPE_SMALL_SOUTH = Shapes.or(SHAPE_COMMON, SHAPE_SMALL_PART_NORTH);
    public static final VoxelShape SHAPE_SMALL_NORTH = Shapes.or(SHAPE_COMMON, SHAPE_SMALL_PART_SOUTH);
    public static final VoxelShape SHAPE_SMALL_EAST = Shapes.or(SHAPE_COMMON, SHAPE_SMALL_PART_EAST);
    public static final VoxelShape SHAPE_SMALL_WEST = Shapes.or(SHAPE_COMMON, SHAPE_SMALL_PART_WEST);

    private LampType lampType;
    
    public StreetLampBaseBlock(LampType type) {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(2f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL)            
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)       
            .setValue(LIT, Boolean.valueOf(false))       
        );

        this.lampType = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE_COMMON;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (lampType == LampType.SMALL || lampType == LampType.SMALL_DOUBLE) {
            switch((Direction)pState.getValue(FACING)) {
                case NORTH:
                   return lampType == LampType.SMALL ? SHAPE_SMALL_NORTH : Shapes.or(SHAPE_SMALL_NORTH, SHAPE_SMALL_SOUTH);
                case SOUTH:
                    return lampType == LampType.SMALL ? SHAPE_SMALL_SOUTH : Shapes.or(SHAPE_SMALL_NORTH, SHAPE_SMALL_SOUTH);
                case EAST:
                    return lampType == LampType.SMALL ? SHAPE_SMALL_EAST : Shapes.or(SHAPE_SMALL_EAST, SHAPE_SMALL_WEST);
                case WEST:
                    return lampType == LampType.SMALL ? SHAPE_SMALL_WEST : Shapes.or(SHAPE_SMALL_EAST, SHAPE_SMALL_WEST);
                default:
                   return SHAPE_COMMON;
                }
        } else if (lampType == LampType.NORMAL || lampType == LampType.DOUBLE){
            switch((Direction)pState.getValue(FACING)) {
                case NORTH:
                   return lampType == LampType.NORMAL ? SHAPE_NORTH : Shapes.or(SHAPE_NORTH, SHAPE_SOUTH);
                case SOUTH:
                    return lampType == LampType.NORMAL ? SHAPE_SOUTH : Shapes.or(SHAPE_NORTH, SHAPE_SOUTH);
                case EAST:
                    return lampType == LampType.NORMAL ? SHAPE_EAST : Shapes.or(SHAPE_EAST, SHAPE_WEST);
                case WEST:
                    return lampType == LampType.NORMAL ? SHAPE_WEST : Shapes.or(SHAPE_EAST, SHAPE_WEST);
                default:
                   return SHAPE_COMMON;
                }
        } 

        return SHAPE_COMMON;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (item instanceof WrenchItem) {
            if (!pLevel.isClientSide) {
                if (pLevel.getBlockEntity(pPos) instanceof StreetLampBlockEntity blockEntity && blockEntity.getOnTime() != blockEntity.getOffTime()) {
                    if (!pLevel.isClientSide) {
                        pPlayer.displayClientMessage(new TranslatableComponent("block.trafficcraft.street_lamp.use.error_scheduled"), false);  
                        return InteractionResult.FAIL;
                    }
                } else {                    
                    pLevel.setBlockAndUpdate(pPos, pState.setValue(LIT, !pState.getValue(LIT)));
                }
            } else {            
                pLevel.playSound(pPlayer, pPos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, 0.5f);
            }
            return InteractionResult.SUCCESS;
        }  

        
        
        return InteractionResult.FAIL;
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
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
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
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, LIT, WATERLOGGED);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }   

    public enum LampType {
        NORMAL,
        SMALL,
        DOUBLE,
        SMALL_DOUBLE
    }

    /* BLOCK ENTITY */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new StreetLampBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        //return createTickerHelper(pBlockEntityType, ModBlockEntities.STREET_LAMP_BLOCK_ENTITY.get(), StreetLampBlockEntity::tick);
        if (!pLevel.isClientSide) {
            return (level, pos, state, blockEntity) -> {
                long tickCount = level.getGameTime();
                if (tickCount % 50 == 0) { // führe die Tick-Methode nur alle 5 Ticks aus
                    ((StreetLampBlockEntity) blockEntity).tick(level, pos, state);
                }
            };
        }
        return null;
    }  
}
