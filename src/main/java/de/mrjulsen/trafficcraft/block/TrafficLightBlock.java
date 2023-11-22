package de.mrjulsen.trafficcraft.block;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.data.ColorableBlock;
import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.data.TrafficLightDirection;
import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.data.TrafficLightVariant;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.ILinkerItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
    public static final EnumProperty<TrafficLightVariant> VARIANT = EnumProperty.create("variant", TrafficLightVariant.class);
    public static final EnumProperty<TrafficLightDirection> DIRECTION = EnumProperty.create("direction", TrafficLightDirection.class);
    public static final EnumProperty<TrafficLightMode> MODE = EnumProperty.create("mode", TrafficLightMode.class);

    public static final VoxelShape SHAPE_COMMON = Block.box(7, 0, 7, 9, 16, 9);

    public static final VoxelShape SHAPE_PART_NORTH = Block.box(4, 4.5d, 1, 12, 16, 6);
    public static final VoxelShape SHAPE_PART_SOUTH = Block.box(4, 4.5d, 10, 12, 16, 15);
    public static final VoxelShape SHAPE_PART_EAST = Block.box(10, 4.5d, 4, 15, 16, 12);
    public static final VoxelShape SHAPE_PART_WEST = Block.box(1, 4.5d, 4, 6, 16, 12);
    
    public static final VoxelShape SHAPE_EXTRA_PART_NORTH = Block.box(4, -0.5d, 1, 12, 5, 6);
    public static final VoxelShape SHAPE_EXTRA_PART_SOUTH = Block.box(4, -0.5d, 10, 12, 5, 15);
    public static final VoxelShape SHAPE_EXTRA_PART_EAST = Block.box(10, -0.5d, 4, 15, 5, 12);
    public static final VoxelShape SHAPE_EXTRA_PART_WEST = Block.box(1, -0.5d, 4, 6, 5, 12);

    public static final VoxelShape SHAPE_NORTH = Shapes.or(SHAPE_COMMON, SHAPE_PART_NORTH);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(SHAPE_COMMON, SHAPE_PART_SOUTH);
    public static final VoxelShape SHAPE_EAST = Shapes.or(SHAPE_COMMON, SHAPE_PART_EAST);
    public static final VoxelShape SHAPE_WEST = Shapes.or(SHAPE_COMMON, SHAPE_PART_WEST);

    public TrafficLightBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(5f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.ANVIL)
            .lightLevel((state) -> {
                return state.getValue(MODE) == TrafficLightMode.OFF ? 0 : 1;
            })
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)            
            .setValue(VARIANT, TrafficLightVariant.NORMAL)
            .setValue(DIRECTION, TrafficLightDirection.NORMAL)
            .setValue(MODE, TrafficLightMode.OFF)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(VARIANT, TrafficLightVariant.NORMAL).setValue(DIRECTION, TrafficLightDirection.NORMAL).setValue(MODE, TrafficLightMode.OFF));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE_COMMON;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        TrafficLightVariant variant = pState.getValue(VARIANT);
        switch((Direction)pState.getValue(FACING)) {
            case NORTH:
               return variant == TrafficLightVariant.NORMAL ? Shapes.or(SHAPE_NORTH, SHAPE_EXTRA_PART_NORTH) : SHAPE_NORTH;
            case SOUTH:
                return variant == TrafficLightVariant.NORMAL ? Shapes.or(SHAPE_SOUTH, SHAPE_EXTRA_PART_SOUTH) : SHAPE_SOUTH;
            case EAST:
                return variant == TrafficLightVariant.NORMAL ? Shapes.or(SHAPE_EAST, SHAPE_EXTRA_PART_EAST) : SHAPE_EAST;
            case WEST:
                return variant == TrafficLightVariant.NORMAL ? Shapes.or(SHAPE_WEST, SHAPE_EXTRA_PART_WEST) : SHAPE_WEST;
            default:
               return SHAPE_COMMON;
        }
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
        pBuilder.add(WATERLOGGED, FACING, VARIANT, DIRECTION, MODE);
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

        boolean isValidLinker = (item instanceof ILinkerItem && ((ILinkerItem)item).isTargetBlockAccepted(this));

        if (item == null || !isValidLinker) {    
            if(pLevel.isClientSide && item instanceof WrenchItem) {
                if(!pPlayer.isShiftKeyDown())
                    ClientWrapper.showTrafficLightConfigScreen(pPos, pLevel);
                    
                return InteractionResult.SUCCESS;
            }
        } else if (isValidLinker) {
            /*
            if (pLevel.isLoaded(pPos)) {
                if (pLevel.getBlockEntity(pPos) instanceof TrafficLightBlockEntity blockEntity && item instanceof TrafficLightLinkerItem linker) {
                    CompoundTag tag = null;
                    if ((tag = linker.doesContainValidLinkData(stack)) != null) {
                        String dim = tag.getString("dim");
                        int x = tag.getInt("x");
                        int y = tag.getInt("y");
                        int z = tag.getInt("z");
                        if (pLevel.dimension().location().toString().equals(dim)) {
                            if (pLevel.isClientSide) {                        
                                pPlayer.sendMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.link", pPos.toShortString(), dim), pPlayer.getUUID());   
                            } else { 
                                blockEntity.linkTo(new BlockPos(x, y, z), dim);    
                            }
                        } else {
                            if (pLevel.isClientSide) {
                                pPlayer.sendMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.invalid_dim"), pPlayer.getUUID());
                            }
                        }
                    } else {
                        if (pLevel.isClientSide) {
                            pPlayer.sendMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.invalid_link"), pPlayer.getUUID());
                        }
                        blockEntity.clearLink();
                    }
                }
            } else {
                if (pLevel.isClientSide) {
                    pPlayer.sendMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.target_not_loaded"), pPlayer.getUUID());
                }
            }
            */
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
        
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            if (pLevel.getBlockEntity(pPos) instanceof TrafficLightBlockEntity blockEntity) {
                if (pLevel.hasNeighborSignal(pPos)) {
                    if (blockEntity.getSchedule().getTrigger() == TrafficLightTrigger.REDSTONE && !blockEntity.isPowered()) {
                        blockEntity.setPowered(true);
                        blockEntity.startSchedule(false);
                    }
                } else {
                    blockEntity.setPowered(false);
                }                    
            }
        }
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
