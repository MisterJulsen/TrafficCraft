package de.mrjulsen.trafficcraft.block.colors;

import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IPaintableBlock {    
    
    default void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (!(item instanceof BrushItem)) {
            return;
        }

        if (pLevel.getBlockEntity(pPos) instanceof IColorStorageBlockEntity blockEntity) {
            if (blockEntity.getColor() == PaintColor.NONE) {
                return;
            }
            blockEntity.setColor(PaintColor.NONE);
            pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
        } 
    }

    default InteractionResult onSetColor(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        ItemStack stack = pContext.getItemInHand();

        if (level.getBlockEntity(pos) instanceof IColorStorageBlockEntity blockEntity) {
            if (!level.isClientSide) {
                blockEntity.setColor(BrushItem.getColor(stack));
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    default InteractionResult update(UseOnContext pContext) {
        return InteractionResult.FAIL;
    }

    default int getDefaultColor() {
        return 0xFFFFFFFF;
    }
}
