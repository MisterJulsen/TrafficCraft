package de.mrjulsen.trafficcraft.block;

import java.util.Random;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.entity.ModBlockEntities;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightRequestButtonBlockEntity;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.properties.ILinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TrafficLightRequestButtonBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final int PRESS_DURATION = 30;

    public static final VoxelShape SHAPE_COMMON = Block.box(6, 0, 6, 10, 16, 10);

    public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(5, 1, 3, 11, 10, 8), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(5, 1, 8, 11, 10, 13), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(8, 1, 5, 13, 10, 11), SHAPE_COMMON);
    public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(3, 1, 5, 8, 10, 11), SHAPE_COMMON);
    
    public TrafficLightRequestButtonBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(2f)
            .requiresCorrectToolForDrops()
            .noOcclusion()            
            .sound(SoundType.METAL)            
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)       
            .setValue(POWERED, false)
            .setValue(ACTIVATED, false)              
            .setValue(WATERLOGGED, false)
        );        
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch((Direction)pState.getValue(FACING)) {
            case NORTH:
               return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
               return SHAPE_COMMON;
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pResult) {
        if (!pState.getValue(POWERED) && !pState.getValue(ACTIVATED)) {
            Direction direction = pResult.getDirection();
            BlockPos blockpos = pResult.getBlockPos();
            boolean flag = this.isProperHit(pState, direction, pResult.getLocation().y - (double)blockpos.getY());
            if (flag) {
                this.press(pState, pLevel, pPos);
                pLevel.gameEvent(pPlayer, GameEvent.BLOCK_PRESS, pPos);
                return InteractionResult.SUCCESS;
            }
        }

        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (item instanceof BrushItem) {
            return InteractionResult.FAIL;
        }

        boolean isValidLinker = (item instanceof ILinkerItem && ((ILinkerItem)item).isTargetBlockAccepted(this));
        
        if (isValidLinker) {
            if (pLevel.isLoaded(pPos)) {
                if (pLevel.getBlockEntity(pPos) instanceof TrafficLightRequestButtonBlockEntity blockEntity && item instanceof TrafficLightLinkerItem linker) {
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

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private boolean isProperHit(BlockState state, Direction pDirection, double pDistanceY) {
        if (pDirection ==  state.getValue(FACING) && !(pDistanceY > (double)0.625F) && !(pDistanceY < (double)0.0625F)) {
            return true;
        } else {
           return false;
        }
     }

    public void press(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 3);
        pLevel.playSound(null, pPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.90000004F);
        pLevel.scheduleTick(pPos, this, PRESS_DURATION);
    }
    
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand) {
        if (pState.getValue(POWERED)) {
            if (pLevel.getBlockEntity(pPos) instanceof TrafficLightRequestButtonBlockEntity blockEntity && blockEntity.isValidLinked()) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, false).setValue(ACTIVATED, true));
                blockEntity.activate();               
            } else {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, false).setValue(ACTIVATED, false));
            }

            pLevel.playSound(null, pPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.75F);
            pLevel.gameEvent(GameEvent.BLOCK_UNPRESS, pPos);  
        }
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
        pBuilder.add(FACING, POWERED, ACTIVATED, WATERLOGGED);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    } 

    
    
    /* BLOCK ENTITY */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrafficLightRequestButtonBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.TRAFFIC_LIGHT_REQUEST_BUTTON_BLOCK_ENTITY.get(), TrafficLightRequestButtonBlockEntity::tick);
    }
}
