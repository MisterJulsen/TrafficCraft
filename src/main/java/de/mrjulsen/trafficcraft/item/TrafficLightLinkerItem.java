package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightRequestButtonBlockEntity;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
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

    public static final String NBT_LINK_TARGET = "LinkTargetLocation";

    public TrafficLightLinkerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) { 
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        
        if (!player.isShiftKeyDown()) {
            if (isSourceBlockAccepted(pContext.getLevel().getBlockState(clickedPos).getBlock())) {            
                if (!level.isClientSide) {
                    CompoundTag compound = pContext.getItemInHand().getOrCreateTag();
                    compound.put(NBT_LINK_TARGET, new Location(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), level.dimension().location().toString()).toNbt());
                    
                    player.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.set", clickedPos.toShortString(), level.dimension().location()).withStyle(ChatFormatting.GREEN), true);
                }
                return InteractionResult.SUCCESS;
            } else if (isTargetBlockAccepted(pContext.getLevel().getBlockState(clickedPos).getBlock())) {                        
                CompoundTag nbt = doesContainValidLinkData(pContext.getItemInHand());
                if (nbt == null) {
                    return InteractionResult.FAIL;
                }

                Location linkLoc = Location.fromNbt(nbt.getCompound(NBT_LINK_TARGET));
                
                if (!pContext.getLevel().dimension().location().toString().equals(linkLoc.dimension)) {
                    player.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.wrong_dimension").withStyle(ChatFormatting.RED), true);
                }

                if (pContext.getLevel().getBlockState(clickedPos).getBlock() instanceof TrafficLightRequestButtonBlock && pContext.getLevel().getBlockEntity(clickedPos) instanceof TrafficLightRequestButtonBlockEntity blockEntity) {
                    blockEntity.linkTo(linkLoc);
                    player.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.link", clickedPos.toShortString(), level.dimension().location()).withStyle(ChatFormatting.GREEN), true);
                } else {                        
                    if (pContext.getLevel().isLoaded(linkLoc.getLocationBlockPos()) && isSourceBlockAccepted(pContext.getLevel().getBlockState(linkLoc.getLocationBlockPos()).getBlock())) {
                        if (pContext.getLevel().getBlockEntity(linkLoc.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
                            BlockPos pos = pContext.getClickedPos();
                            String dim = pContext.getLevel().dimension().location().toString();
                            blockEntity.addTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), dim));
                            player.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.link", clickedPos.toShortString(), level.dimension().location()).withStyle(ChatFormatting.GREEN), true);
                        }
                    } else {
                        player.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.target_not_loaded").withStyle(ChatFormatting.RED), true);
                    }
                }

                return InteractionResult.SUCCESS;
            } 
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
                    if (tag.contains(NBT_LINK_TARGET)) {
                        tag.remove(NBT_LINK_TARGET);
                    }
                }

                
                pPlayer.displayClientMessage(GuiUtils.translate("item.trafficcraft.traffic_light_linker.use.clear"), true);
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
            Location loc = Location.fromNbt(tag.getCompound(NBT_LINK_TARGET));
            pTooltipComponents.add(GuiUtils.translate("item.trafficcraft.traffic_light_linker.tooltip",
                Double.toString(loc.x),
                Double.toString(loc.y),
                Double.toString(loc.z),
                loc.dimension,
                new KeybindComponent("key.sneak"),
                new KeybindComponent("key.use")
            ));
        } else {
            pTooltipComponents.add(GuiUtils.translate("item.trafficcraft.traffic_light_linker.tooltip.nolink").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return doesContainValidLinkData(pStack) != null || super.isFoil(pStack);
    }

    public CompoundTag doesContainValidLinkData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_LINK_TARGET) ? tag : null;
    }

    @Override
    public boolean isTargetBlockAccepted(Block block) {
        return block.equals(ModBlocks.TRAFFIC_LIGHT.get()) || block.equals(ModBlocks.TRAFFIC_LIGHT_REQUEST_BUTTON.get());
    }

    @Override
    public boolean isSourceBlockAccepted(Block block) {
        return block == ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get();
    }

}
