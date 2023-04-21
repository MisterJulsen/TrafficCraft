package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;

public class PaintedAsphaltBlock extends RoadBlock {

    private Block pickupBlock;

    public PaintedAsphaltBlock(RoadType type, Block pickupBlock) {
        super(Properties.of(Material.STONE)
            .strength(1.5f)
            .requiresCorrectToolForDrops()
        , type);

        this.pickupBlock = pickupBlock;
    }    

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (pickupBlock == null || pickupBlock == this) {
            ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
            stack.setTag(null);
            return stack;
        }

        ItemStack stack = this.pickupBlock.getCloneItemStack(state, target, level, pos, player);
        stack.setTag(null);
        return stack;
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

        if (this == ModBlocks.ASPHALT.get() || this == ModBlocks.CONCRETE.get()) {
            return;
        }

        switch (this.getDefaultRoadType()) {
            case ASPHALT:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.ASPHALT.get().defaultBlockState());
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            case CONCRETE:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.CONCRETE.get().defaultBlockState());
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            default:
                break;
        }
    }

    
}
