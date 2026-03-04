package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class ManholeCoverBlock extends ManholeBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    
    public static final VoxelShape SHAPE_COVER = Block.box(1, 14, 1, 15, 16, 15);    

    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);

    public ManholeCoverBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(OPEN, false)   
        );
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = SHAPE_BASE;
        
        switch((Direction)pState.getValue(FACING)) {
            case NORTH:
                shape = Shapes.or(shape, NORTH_AABB);
                break;
            case SOUTH:
                shape = Shapes.or(shape, SOUTH_AABB);
                break;
            case WEST:
                shape = Shapes.or(shape, WEST_AABB);
                break;
            case EAST:
            default:
                shape = Shapes.or(shape, EAST_AABB);
                break;
        }

        return pState.getValue(OPEN) ? shape : Shapes.or(shape, SHAPE_COVER);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = SHAPE_BASE;
        return pState.getValue(OPEN) ? shape : Shapes.or(shape, SHAPE_COVER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack item = player.getInventory().getSelected();
        if (!item.is(ModTags.WRENCHES)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        
        state = state.cycle(OPEN);
        level.setBlockAndUpdate(pos, state);
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1, 0.5F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(OPEN);
    }
    
}
