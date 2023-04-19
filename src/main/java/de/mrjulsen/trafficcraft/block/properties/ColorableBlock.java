package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.block.colors.IColoredBlock;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public abstract class ColorableBlock extends Block implements IColoredBlock {

    public ColorableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(COLOR, PaintColor.NONE)
        );
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(COLOR);
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        onRemoveColor(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (!(item instanceof BrushItem)) {
            return;
        }

        if (pState.getValue(IColoredBlock.COLOR) == PaintColor.NONE) {
            return;
        }

        pLevel.setBlockAndUpdate(pPos, pState.setValue(IColoredBlock.COLOR, PaintColor.NONE));
        pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
    }

}
