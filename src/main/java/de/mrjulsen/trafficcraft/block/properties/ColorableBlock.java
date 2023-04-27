package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ColorableBlock extends BaseEntityBlock implements IPaintableBlock {

    public ColorableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        onRemoveColor(pState, pLevel, pPos, pPlayer);
    }

    public void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (!(item instanceof BrushItem)) {
            return;
        }

        if (pLevel.getBlockEntity(pPos) instanceof ColoredBlockEntity blockEntity) {
            if (blockEntity.getColor() == PaintColor.NONE) {
                return;
            }
            blockEntity.setColor(PaintColor.NONE);
            pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
        } 
    }

    public void onSetColor(Level pLevel, BlockPos pPos, PaintColor color) {
        if (pLevel.getBlockEntity(pPos) instanceof ColoredBlockEntity blockEntity) {
            if (!pLevel.isClientSide) {
                blockEntity.setColor(color);
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
            }
        } 
    }

    public abstract int getDefaultColor();

    @Override
    public RenderShape getRenderShape(BlockState pState) {        
        return RenderShape.MODEL;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ColoredBlockEntity(pPos, pState);
    }

}
