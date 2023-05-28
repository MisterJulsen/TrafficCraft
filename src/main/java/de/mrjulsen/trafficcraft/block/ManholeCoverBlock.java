package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    
    public ManholeCoverBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(OPEN, false)   
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {        
        return pState.getValue(OPEN) ? SHAPE_BASE : Shapes.or(SHAPE_BASE, SHAPE_COVER);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {    
        Item item = pPlayer.getInventory().getSelected().getItem();
        if (!(item instanceof WrenchItem)) {
            return InteractionResult.PASS;
        }
        
        pState = pState.cycle(OPEN);
        pLevel.setBlockAndUpdate(pPos, pState);
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        pLevel.playSound(null, pPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1, 0.5F);
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(OPEN);
    }
    
}
