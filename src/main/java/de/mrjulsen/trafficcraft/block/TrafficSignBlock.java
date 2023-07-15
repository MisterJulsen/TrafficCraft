package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.client.TrafficSignClient;
import de.mrjulsen.trafficcraft.block.properties.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TrafficSignBlock extends Block implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;


    private static final VoxelShape SHAPE_COMMON = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(SHAPE_COMMON, Block.box(0, 0, 9, 16, 16, 9.5D));    
    private static final VoxelShape SHAPE_NORTH = Shapes.or(SHAPE_COMMON, Block.box(0, 0, 6.5D, 16, 16, 7));
    private static final VoxelShape SHAPE_EAST = Shapes.or(SHAPE_COMMON, Block.box(9, 0, 0, 9.5D, 16, 16));
    private static final VoxelShape SHAPE_WEST = Shapes.or(SHAPE_COMMON, Block.box(6.5D, 0, 0, 7, 16, 16));


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
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(ModBlocks.CIRCLE_TRAFFIC_SIGN.get());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
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
        pBuilder.add(FACING, WATERLOGGED);
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

    protected abstract int getType(BlockState state);
    protected abstract TrafficSignShape getSignShape();



    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {        
        Item item = pPlayer.getInventory().getSelected().getItem();

        if (item instanceof BrushItem) {
            return InteractionResult.FAIL;
        }

        if(pLevel.isClientSide)
        {
            if (item instanceof WrenchItem) {
                if(!pPlayer.isShiftKeyDown()) {                
                    TrafficSignClient.showGui(getType(pState), 0, getSignShape(), pPos, pLevel, pPlayer);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;

    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return pState.getValue(FACING).getOpposite() == pDirection;
    }
}
