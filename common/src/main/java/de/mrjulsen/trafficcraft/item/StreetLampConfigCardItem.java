package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.TimeUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.components.StreetLampComponent;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StreetLampConfigCardItem extends Item implements IUseDataComponent<StreetLampComponent> {

    public static final int DEFAULT_TURN_ON_TIME = 18500;
    public static final int DEFAULT_TURN_OFF_TIME = 5500;

    private static final Component textClear = TextUtils.translate("item.trafficcraft.street_lamp_config_card.use.clear");
    private static final Component textErrorTimeEqual = TextUtils.translate("item.trafficcraft.street_lamp_config_card.use.error_same_time").withStyle(ChatFormatting.RED);
    private static final Component textApply = TextUtils.translate("item.trafficcraft.street_lamp_config_card.use.set").withStyle(ChatFormatting.GREEN);
    private static final Component textRemove = TextUtils.translate("item.trafficcraft.street_lamp_config_card.use.unset").withStyle(ChatFormatting.RED);
    private static final String keyTurnOn = "item.trafficcraft.street_lamp_config_card.tooltip.turn_on_time";
    private static final String keyTurnOff = "item.trafficcraft.street_lamp_config_card.tooltip.turn_off_time";

    public StreetLampConfigCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);        

        if (player.isShiftKeyDown()) {
            stack.remove(ModDataComponents.STREET_LAMP_COMPONENT.get());
            player.displayClientMessage(textClear, true);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        } else {
            if (level.isClientSide) {
                StreetLampComponent comp = getComponent(stack);
                ClientWrapper.showStreetLampScheduleScreen(comp.turnOnTime(), comp.turnOffTime(), comp.timeFormat());
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }

        return super.use(level, player, hand);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (!hasComponent(stack)) {
            return;
        }

        StreetLampComponent comp = getComponent(stack);
        tooltipComponents.add(TextUtils.translate(keyTurnOn, TimeUtils.parseTime(comp.turnOnTime(), comp.timeFormat())));
        tooltipComponents.add(TextUtils.translate(keyTurnOff, TimeUtils.parseTime(comp.turnOffTime(), comp.timeFormat()))); 
    }
    
    @Override
    public boolean isFoil(ItemStack pStack) {
        return hasComponent(pStack) || super.isFoil(pStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        ItemStack stack = pContext.getItemInHand();

        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        Player player = pContext.getPlayer();

        if (!player.isShiftKeyDown() && state.getBlock() instanceof StreetLampBaseBlock && level.getBlockEntity(pos) instanceof StreetLampBlockEntity blockEntity) {
            if (!level.isClientSide) {
                if (hasComponent(stack)) {
                    StreetLampComponent comp = getComponent(stack);
                    if (comp.turnOnTime() == comp.turnOffTime()) {
                        player.displayClientMessage(textErrorTimeEqual, false);                        
                        return InteractionResult.FAIL;
                    }
                    blockEntity.setOnTime((int)TimeUtils.shiftDayTimeToMinecraftTicks(comp.turnOnTime()));
                    blockEntity.setOffTime((int)TimeUtils.shiftDayTimeToMinecraftTicks(comp.turnOffTime()));
                    player.displayClientMessage(textApply, true);
                    DLUtils.giveAdvancement((ServerPlayer)player, TrafficCraft.MOD_ID, "street_lamp_config", "requirement");
                } else {
                    stack.remove(ModDataComponents.STREET_LAMP_COMPONENT.get());
                    player.displayClientMessage(textRemove, true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }

    @Override
    public DataComponentType<StreetLampComponent> getComponentType() {
        return ModDataComponents.STREET_LAMP_COMPONENT.get();
    }

    @Override
    public StreetLampComponent emptyComponent() {
        return StreetLampComponent.empty();
    }
}