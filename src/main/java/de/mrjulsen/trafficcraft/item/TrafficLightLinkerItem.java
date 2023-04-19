package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.item.properties.ILinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TrafficLightLinkerItem extends Item implements ILinkerItem {

    public TrafficLightLinkerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) { 
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        
        if (!player.isShiftKeyDown() && pContext.getLevel().getBlockState(clickedPos).getBlock() == ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get()) {            
            if (!level.isClientSide) {
                CompoundTag compound = pContext.getItemInHand().getOrCreateTag();
                compound.putInt("x", clickedPos.getX());
                compound.putInt("y", clickedPos.getY());
                compound.putInt("z", clickedPos.getZ());
                compound.putString("dim", level.dimension().location().toString());

                player.displayClientMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.set", clickedPos.toShortString(), level.dimension().location()), false);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (pPlayer.isShiftKeyDown()) {
            Level level = pPlayer.getLevel();
            if (!level.isClientSide) {
                if (itemstack.getTag() != null) {
                    CompoundTag tag = itemstack.getTag();
                    if (tag.contains("x"))
                        tag.remove("x");
                    if (tag.contains("y"))
                        tag.remove("y");
                    if (tag.contains("z"))
                        tag.remove("z");
                    if (tag.contains("dim"))
                        tag.remove("dim");
                }

                
                pPlayer.displayClientMessage(new TranslatableComponent("item.trafficcraft.traffic_light_linker.use.clear"), true);
            }
            return InteractionResultHolder.success(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = null;
        if ((tag = doesContainValidLinkData(pStack)) != null) {
            pTooltipComponents.add(new TranslatableComponent("item.trafficcraft.traffic_light_linker.tooltip",
                Integer.toString(tag.getInt("x")),
                Integer.toString(tag.getInt("y")),
                Integer.toString(tag.getInt("z")),
                tag.getString("dim"),
                new KeybindComponent("key.sneak"),
                new KeybindComponent("key.use")
            ));
        } else {
            pTooltipComponents.add(new TranslatableComponent("item.trafficcraft.traffic_light_linker.tooltip.nolink"));
        }
    }

    public CompoundTag doesContainValidLinkData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("x") && tag.contains("y") && tag.contains("z") && tag.contains("dim") ? tag : null;
    }

    @Override
    public boolean isTargetBlockAccepted(Block block) {
        return block == ModBlocks.TRAFFIC_LIGHT.get() || block == ModBlocks.TRAFFIC_LIGHT_REQUEST_BUTTON.get();
    }

    @Override
    public boolean isSourceBlockAccepted(Block block) {
        return block == ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get();
    }

}
