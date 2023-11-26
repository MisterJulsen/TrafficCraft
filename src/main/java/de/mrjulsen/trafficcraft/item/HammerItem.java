package de.mrjulsen.trafficcraft.item;

import javax.annotation.Nonnull;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Rotation;
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
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(pState));
            } else {
                if (!(player.isCreative() || player.isSpectator())) {
                    Block.dropResources(pState.getBlock().defaultBlockState(), level, pos);
                }
                itemstack.hurtAndBreak(1, player, (p) -> {
                    player.broadcastBreakEvent(player.getItemInHand(InteractionHand.MAIN_HAND) == itemstack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                });
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
            if (Utils.rotateBlock(level, pos, Rotation.CLOCKWISE_90)) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 2.0f, false);
                level.levelEvent(pContext.getPlayer(), LevelEvent.PARTICLES_SCRAPE, pos, Block.getId(pContext.getLevel().getBlockState(pContext.getClickedPos())));
                pContext.getPlayer().getCooldowns().addCooldown(pContext.getItemInHand().getItem(), 10);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }

    @Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack) {
		ItemStack container = stack.copy();
		if (container.hurt(1, Constants.RANDOM, null))
			return ItemStack.EMPTY;
		else
			return container;
	}

	@Override
	public boolean hasContainerItem(@Nonnull ItemStack stack) {
		return true;
	}
}