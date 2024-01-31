package de.mrjulsen.trafficcraft.item;

import java.util.List;
import java.util.Arrays;

import de.mrjulsen.mcdragonlib.common.IIterableEnum;
import de.mrjulsen.mcdragonlib.common.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightRequestButtonBlockEntity;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.LinkerModePacket;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
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
import net.minecraftforge.registries.ForgeRegistries;

public class TrafficLightLinkerItem extends Item implements ILinkerItem, IScrollEventItem {

    public static final String NBT_LINK_TARGET = "LinkTargetLocation";
    public static final String NBT_MODE = "Mode";
    public static final String NBT_BLOCK = "Block";

    private static final Component textNoLink = Utils.translate("item.trafficcraft.traffic_light_linker.tooltip.nolink").withStyle(ChatFormatting.GRAY);
    private static final Component textNotLoaded = Utils.translate("item.trafficcraft.traffic_light_linker.use.target_not_loaded").withStyle(ChatFormatting.RED);
    private static final Component textClear = Utils.translate("item.trafficcraft.traffic_light_linker.use.clear");
    private static final Component textTooltipInstruction = Utils.translate("item.trafficcraft.traffic_light_linker.tooltip_instruction").withStyle(ChatFormatting.ITALIC);
    private static final String keySet = "item.trafficcraft.traffic_light_linker.use.set";
    private static final String keyWrongDim = "item.trafficcraft.traffic_light_linker.use.wrong_dimension";
    private static final String keySetLink = "item.trafficcraft.traffic_light_linker.use.link";
    private static final String keyRemoveLink = "item.trafficcraft.traffic_light_linker.use.unlink";
    private static final String keyTooltipLocation = "item.trafficcraft.traffic_light_linker.tooltip_location";
    private static final String keyTooltipBlock = "item.trafficcraft.traffic_light_linker.tooltip_block";
    private static final String keyTooltipMode = "item.trafficcraft.traffic_light_linker.tooltip_mode";

    public TrafficLightLinkerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) { 
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        Player player = pContext.getPlayer();
        
        if (!player.isShiftKeyDown()) {
            Block clickedBlock = pContext.getLevel().getBlockState(clickedPos).getBlock();
            CompoundTag nbt = doesContainValidLinkData(pContext.getItemInHand());
            if (nbt == null && isSourceBlockAccepted(clickedBlock)) {
                // Link
                if (!level.isClientSide) {
                    CompoundTag compound = pContext.getItemInHand().getOrCreateTag();
                    compound.put(NBT_LINK_TARGET, new Location(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), level.dimension().location().toString()).toNbt());
                    compound.putString(NBT_BLOCK, ForgeRegistries.BLOCKS.getKey(clickedBlock).toString());
                    player.displayClientMessage(Utils.translate(keySet, clickedPos.toShortString(), level.dimension().location()).withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResult.SUCCESS;
            } else if (isTargetBlockAccepted(clickedBlock)) {                        
                if (nbt == null) {
                    return InteractionResult.FAIL;
                }                

                Location linkLoc = Location.fromNbt(nbt.getCompound(NBT_LINK_TARGET));
                LinkerMode mode = LinkerMode.getByIndex(nbt.getInt(NBT_MODE));
                
                if (!pContext.getLevel().dimension().location().toString().equals(linkLoc.dimension)) {
                    player.displayClientMessage(Utils.translate(keyWrongDim).withStyle(ChatFormatting.RED), true);
                }

                if (clickedBlock instanceof TrafficLightRequestButtonBlock && pContext.getLevel().getBlockEntity(clickedPos) instanceof TrafficLightRequestButtonBlockEntity blockEntity) {
                    switch (mode) {
                        case UNLINK:
                            blockEntity.clearLink();
                            player.displayClientMessage(Utils.translate(keyRemoveLink, linkLoc.getLocationBlockPos().toShortString(), level.dimension().location()).withStyle(ChatFormatting.RED), true);
                            break;
                        case LINK:
                        default:
                            blockEntity.linkTo(linkLoc);
                            player.displayClientMessage(Utils.translate(keySetLink, linkLoc.getLocationBlockPos().toShortString(), level.dimension().location()).withStyle(ChatFormatting.GREEN), true);
                            break;
                    }
                } else {                        
                    if (pContext.getLevel().isLoaded(linkLoc.getLocationBlockPos()) && isSourceBlockAccepted(pContext.getLevel().getBlockState(linkLoc.getLocationBlockPos()).getBlock())) {
                        if (pContext.getLevel().getBlockEntity(linkLoc.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
                            BlockPos pos = pContext.getClickedPos();
                            String dim = pContext.getLevel().dimension().location().toString();

                            switch (mode) {
                                case UNLINK:                                
                                    blockEntity.removeTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), dim));
                                    player.displayClientMessage(Utils.translate(keyRemoveLink, linkLoc.getLocationBlockPos().toShortString(), level.dimension().location()).withStyle(ChatFormatting.RED), true);
                                    break;
                                case LINK:
                                default:
                                    blockEntity.addTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), dim));
                                    player.displayClientMessage(Utils.translate(keySetLink, linkLoc.getLocationBlockPos().toShortString(), level.dimension().location()).withStyle(ChatFormatting.GREEN), true);
                                    break;
                            }
                        }
                    } else {
                        player.displayClientMessage(textNotLoaded, true);
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
            Level level = pPlayer.level();
            if (!level.isClientSide) {
                if (itemstack.getTag() != null) {
                    CompoundTag tag = itemstack.getTag();
                    tag.remove(NBT_LINK_TARGET);
                    tag.remove(NBT_BLOCK);
                }                
                pPlayer.displayClientMessage(textClear, true);
            }
            return InteractionResultHolder.success(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = null;
        if ((tag = doesContainValidLinkData(pStack)) != null) {
            Location loc = Location.fromNbt(tag.getCompound(NBT_LINK_TARGET));
            pTooltipComponents.add(Utils.translate(keyTooltipLocation, Integer.toString((int)loc.x), Integer.toString((int)loc.y), Integer.toString((int)loc.z), loc.dimension));            
        } else {
            pTooltipComponents.add(textNoLink);
        }
        
        CompoundTag nbt = pStack.getOrCreateTag();
        LinkerMode mode = LinkerMode.getByIndex(nbt.getInt(NBT_MODE));
        if (nbt.contains(NBT_BLOCK)) {
            pTooltipComponents.add(Utils.translate(keyTooltipBlock, BuiltInRegistries.BLOCK.get(new ResourceLocation(nbt.getString(NBT_BLOCK))).getName().getString()));
        }
        pTooltipComponents.add(Utils.translate(keyTooltipMode, Utils.translate(mode.getValueTranslationKey(ModMain.MOD_ID)), Utils.translate(mode.getValueTranslationKey(ModMain.MOD_ID))));
        pTooltipComponents.add(textTooltipInstruction);
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
        return block.equals(ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get()) || block.equals(ModBlocks.TRAFFIC_LIGHT.get());
    }

    @Override
    public boolean mouseScroll(Player player, ItemStack itemStack, double scrollDelta, double mouseX, double mouseY, boolean mouseRightDown, boolean mouseLeftDown, boolean mouseMiddleDown) {
        if (player.isCrouching()) {
            CompoundTag compound = itemStack.getOrCreateTag();
            LinkerMode mode = LinkerMode.LINK;
            if (scrollDelta > 0) {
                mode = LinkerMode.getByIndex(compound.getInt(NBT_MODE)).next();
            } else if (scrollDelta < 0) {
                mode = LinkerMode.getByIndex(compound.getInt(NBT_MODE)).previous();
            }
            setMode(itemStack, mode);
            NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new LinkerModePacket(mode));
            player.displayClientMessage(Utils.translate(keyTooltipMode, Utils.translate(LinkerMode.getByIndex(compound.getInt(NBT_MODE)).getValueTranslationKey(ModMain.MOD_ID)), Utils.translate(LinkerMode.getByIndex(compound.getInt(NBT_MODE)).getValueTranslationKey(ModMain.MOD_ID))), true);
            return true;
        }
        return false;
    }

    public static void setMode(ItemStack item, LinkerMode mode) {
        CompoundTag compound = item.getOrCreateTag();
        compound.putInt(NBT_MODE, mode.getIndex());
    }
    

    public static enum LinkerMode implements StringRepresentable, ITranslatableEnum, IIterableEnum<LinkerMode> {
        LINK(0, "link"),
        UNLINK(1, "unlink");

        private int index;
        private String name;

        LinkerMode(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public static LinkerMode getByIndex(int index) {
            return Arrays.stream(LinkerMode.values()).filter(x -> x.getIndex() == index).findFirst().orElse(LinkerMode.LINK);
        }

        @Override
        public LinkerMode[] getValues() {
            return values();
        }

        @Override
        public String getEnumName() {
            return "linkermode";
        }

        @Override
        public String getEnumValueName() {
            return getName();
        }

        @Override
        public String getSerializedName() {
            return getName();
        }
    }

}
