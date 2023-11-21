package de.mrjulsen.trafficcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HammerItem extends DiggerItem {

    private static final float ATTACK_DAMAGE = 1.0f;
    private static final float ATTACK_SPEED = -3.0f;

    public HammerItem(Properties properties) {
        super(ATTACK_DAMAGE, ATTACK_SPEED, Tiers.IRON, BlockTags.MINEABLE_WITH_PICKAXE, properties
            .stacksTo(1)
            .durability(Tiers.IRON.getUses()));
    }
    
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.getLevel();
        BlockState pState = level.getBlockState(pos);
        if (pState.hasProperty(BlockStateProperties.LAYERS) && pState.getValue(BlockStateProperties.LAYERS) > 1) {
            level.setBlockAndUpdate(pos, pState.setValue(BlockStateProperties.LAYERS, pState.getValue(BlockStateProperties.LAYERS) - 1));
            if (level.isClientSide) {
                level.levelEvent(player, 2001, pos, Block.getId(pState));
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), pState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            } else {
                if (!(player.isCreative() || player.isSpectator())) {
                    Block.dropResources(pState.getBlock().defaultBlockState(), level, pos);
                }
            }
            return true;
        }
        return super.onBlockStartBreak(itemstack, pos, player);
    }
    

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState block = level.getBlockState(pos);

        if (block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            level.setBlockAndUpdate(pos, block.setValue(BlockStateProperties.HORIZONTAL_FACING, block.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise(Axis.Y)));
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }
}