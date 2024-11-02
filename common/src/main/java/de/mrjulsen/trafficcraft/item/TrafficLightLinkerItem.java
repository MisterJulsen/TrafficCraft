package de.mrjulsen.trafficcraft.item;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.mcdragonlib.core.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.core.Location;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightRequestButtonBlockEntity;
import de.mrjulsen.trafficcraft.components.TrafficLightLinkerComponent;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
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

public class TrafficLightLinkerItem extends Item implements ILinkerItem, IScrollEventItem, IUseDataComponent<TrafficLightLinkerComponent> {

    public static final String NBT_LINK_TARGET = "LinkTargetLocation";
    public static final String NBT_MODE = "Mode";
    public static final String NBT_BLOCK = "Block";

    private static final Component textNoLink = TextUtils.translate("item.trafficcraft.traffic_light_linker.tooltip.nolink").withStyle(ChatFormatting.GRAY);
    private static final Component textNotLoaded = TextUtils.translate("item.trafficcraft.traffic_light_linker.use.target_not_loaded").withStyle(ChatFormatting.RED);
    private static final Component textClear = TextUtils.translate("item.trafficcraft.traffic_light_linker.use.clear");
    private static final Component textTooltipInstruction = TextUtils.translate("item.trafficcraft.traffic_light_linker.tooltip_instruction").withStyle(ChatFormatting.ITALIC);
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
        ItemStack stack = pContext.getItemInHand();
        
        if (!player.isShiftKeyDown()) {
            Block clickedBlock = pContext.getLevel().getBlockState(clickedPos).getBlock();
            if (!hasComponent(stack) && isSourceBlockAccepted(clickedBlock)) {
                // Link
                if (!level.isClientSide) {
                    TrafficLightLinkerComponent comp = getComponent(stack);
                    setComponent(stack, new TrafficLightLinkerComponent(
                        Optional.of(new Location(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), level.dimension().location().toString())),
                        comp.mode(),
                        Optional.of(ModBlocks.BLOCKS.getRegistrar().getId(clickedBlock).toString())
                    ));
                    player.displayClientMessage(TextUtils.translate(keySet, clickedPos.toShortString(), level.dimension().location().toString()).withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResult.SUCCESS;
            } else if (isTargetBlockAccepted(clickedBlock)) {                        
                if (!hasComponent(stack)) {
                    return InteractionResult.FAIL;
                }                

                TrafficLightLinkerComponent comp = getComponent(stack);

                Optional<Location> linkLoc = comp.location();
                LinkerMode mode = comp.mode();

                if (!linkLoc.isPresent()) {
                    return InteractionResult.FAIL;
                }
                
                if (!pContext.getLevel().dimension().location().toString().equals(linkLoc.get().dimension)) {
                    player.displayClientMessage(TextUtils.translate(keyWrongDim).withStyle(ChatFormatting.RED), true);
                }

                if (clickedBlock instanceof TrafficLightRequestButtonBlock && pContext.getLevel().getBlockEntity(clickedPos) instanceof TrafficLightRequestButtonBlockEntity blockEntity) {
                    switch (mode) {
                        case UNLINK:
                            blockEntity.clearLink();
                            player.displayClientMessage(TextUtils.translate(keyRemoveLink, linkLoc.get().getLocationBlockPos().toShortString(), level.dimension().location().toString()).withStyle(ChatFormatting.RED), true);
                            break;
                        case LINK:
                        default:
                            blockEntity.linkTo(linkLoc.get());
                            player.displayClientMessage(TextUtils.translate(keySetLink, linkLoc.get().getLocationBlockPos().toShortString(), level.dimension().location().toString()).withStyle(ChatFormatting.GREEN), true);
                            break;
                    }
                } else {                        
                    if (pContext.getLevel().isLoaded(linkLoc.get().getLocationBlockPos()) && isSourceBlockAccepted(pContext.getLevel().getBlockState(linkLoc.get().getLocationBlockPos()).getBlock())) {
                        if (pContext.getLevel().getBlockEntity(linkLoc.get().getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
                            BlockPos pos = pContext.getClickedPos();
                            String dim = pContext.getLevel().dimension().location().toString();

                            switch (mode) {
                                case UNLINK:                                
                                    blockEntity.removeTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), dim));
                                    player.displayClientMessage(TextUtils.translate(keyRemoveLink, linkLoc.get().getLocationBlockPos().toShortString(), level.dimension().location().toString()).withStyle(ChatFormatting.RED), true);
                                    break;
                                case LINK:
                                default:
                                    blockEntity.addTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), dim));
                                    player.displayClientMessage(TextUtils.translate(keySetLink, linkLoc.get().getLocationBlockPos().toShortString(), level.dimension().location().toString()).withStyle(ChatFormatting.GREEN), true);
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
                itemstack.remove(ModDataComponents.TRAFFIC_LIGHT_LINKER_COMPONENT.get());
                pPlayer.displayClientMessage(textClear, true);
            }
            return InteractionResultHolder.success(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        if (hasComponent(stack)) {
            TrafficLightLinkerComponent comp = getComponent(stack);
            Optional<Location> loc = comp.location();
            LinkerMode mode = comp.mode();
            Optional<String> block = comp.targetBlockName();
        
            if (loc.isPresent()) 
                tooltipComponents.add(TextUtils.translate(keyTooltipLocation, Integer.toString((int)loc.get().x), Integer.toString((int)loc.get().y), Integer.toString((int)loc.get().z), loc.get().dimension));            

            if (block.isPresent()) {
                try {
                    ResourceLocation location = ResourceLocation.parse(block.get());
                    tooltipComponents.add(TextUtils.translate(keyTooltipBlock, ModBlocks.BLOCKS.getRegistrar().get(location).getName().getString()));
                } catch (Exception e) {
                    tooltipComponents.add(TextUtils.translate(keyTooltipBlock, TextUtils.text("ERROR").withStyle(ChatFormatting.RED)));
                }
            }
            tooltipComponents.add(TextUtils.translate(keyTooltipMode, TextUtils.translate(mode.getValueTranslationKey(TrafficCraft.MOD_ID)), TextUtils.translate(mode.getValueTranslationKey(TrafficCraft.MOD_ID))));
            tooltipComponents.add(textTooltipInstruction);
        } else {
            tooltipComponents.add(textNoLink);
        }        
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return hasComponent(pStack) && getComponent(pStack).location().isPresent() || super.isFoil(pStack);
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
    public boolean mouseScroll(Player player, ItemStack itemStack, double scrollDelta) {
        if (player.isCrouching()) {
            TrafficLightLinkerComponent comp = getComponent(itemStack);
            LinkerMode mode = LinkerMode.LINK;
            if (scrollDelta > 0) {
                mode = comp.mode().next();
            } else if (scrollDelta < 0) {
                mode = comp.mode().previous();
            }
            setComponent(itemStack, new TrafficLightLinkerComponent(comp.location(), mode, comp.targetBlockName()));
            player.displayClientMessage(TextUtils.translate(keyTooltipMode, TextUtils.translate(mode.getValueTranslationKey(TrafficCraft.MOD_ID)), TextUtils.translate(mode.getValueTranslationKey(TrafficCraft.MOD_ID))), true);
            return true;
        }
        return false;
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


    @Override
    public DataComponentType<TrafficLightLinkerComponent> getComponentType() {
        return ModDataComponents.TRAFFIC_LIGHT_LINKER_COMPONENT.get();
    }

    @Override
    public TrafficLightLinkerComponent emptyComponent() {
        return TrafficLightLinkerComponent.empty();
    }

}
