package de.mrjulsen.trafficcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HammerItem extends Item {

    public HammerItem(Properties properties) {
        super(properties
            .stacksTo(1)
        );
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    
    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if (player.isCreative()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pMiningEntity) {
        if (pState.hasProperty(BlockStateProperties.LAYERS)) {
            pState.setValue(BlockStateProperties.LAYERS, pState.getValue(BlockStateProperties.LAYERS) - 1);
            Block.dropResources(pState, pLevel, pPos);
            return true;
        }
        return super.mineBlock(pStack, pLevel, pState, pPos, pMiningEntity);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState block = level.getBlockState(pos);

        if (block.hasProperty(HorizontalDirectionalBlock.FACING)) {
            block.setValue(HorizontalDirectionalBlock.FACING, block.getValue(HorizontalDirectionalBlock.FACING).getClockWise(Axis.Y));
        }

        return super.useOn(pContext);
    }
}