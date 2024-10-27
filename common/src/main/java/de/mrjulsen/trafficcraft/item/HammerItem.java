package de.mrjulsen.trafficcraft.item;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState block = level.getBlockState(pos);

        if (block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            if (DLUtils.rotateBlock(level, pos, Rotation.CLOCKWISE_90)) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 2.0f, false);
                level.levelEvent(pContext.getPlayer(), LevelEvent.PARTICLES_SCRAPE, pos, Block.getId(pContext.getLevel().getBlockState(pContext.getClickedPos())));
                pContext.getPlayer().getCooldowns().addCooldown(pContext.getItemInHand().getItem(), 10);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {        
        ItemStack container = stack.copy();
        if (container.hurt(1, Constants.RANDOM_SOURCE, null))
            return ItemStack.EMPTY;
        else
            return container;
    }
}