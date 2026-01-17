package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;
import de.mrjulsen.mcdragonlib.util.time.VanillaTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatTicks;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.util.ETimeFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.data.loot.packs.VanillaArchaeologyLoot;
import net.minecraft.nbt.CompoundTag;
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

public class StreetLampConfigCardItem extends Item {

    private static final String NBT_TIME_ON = "turnOnTime";
    private static final String NBT_TIME_OFF = "turnOffTime";
    private static final String NBT_TIME_FORMAT = "timeFormat";

    private static final Component textEmpty = TextUtils.translate("item.trafficcraft.street_lamp_config_card.tooltip.empty").withStyle(ChatFormatting.GRAY);
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

        CompoundTag nbt = null;
        if (player.isShiftKeyDown()) {
            if ((nbt = doesContainValidLinkData(stack)) != null) {
                nbt.remove(NBT_TIME_ON);
                nbt.remove(NBT_TIME_OFF);
            }
            player.displayClientMessage(textClear, true);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        } else {
            if (level.isClientSide) {                
                int turnOn = (int)((VanillaTimeSystem.INSTANCE.getTicksPerDay() - VanillaTimeSystem.INSTANCE.getDaytimeOffset()) + (double)VanillaTimeSystem.INSTANCE.getTicksPerDay() / (24 * 2));
                int turnOff = (int)(VanillaTimeSystem.INSTANCE.getDaytimeOffset() - (double)VanillaTimeSystem.INSTANCE.getTicksPerDay() / (24 * 2));
                int timeFormat = ETimeFormat.HOURS_24.getIndex();
                if ((nbt = doesContainValidLinkData(stack)) != null) {
                    turnOn = nbt.getInt(NBT_TIME_ON);
                    turnOff = nbt.getInt(NBT_TIME_OFF);
                    timeFormat = stack.getOrCreateTag().getInt(NBT_TIME_FORMAT);
                }

                ClientWrapper.showStreetLampScheduleScreen(turnOn, turnOff, ETimeFormat.getByIndex(timeFormat));
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        
        CompoundTag nbt = null;
        if ((nbt = doesContainValidLinkData(stack)) != null) {
            ETimeFormat format = ETimeFormat.getByIndex(nbt.getInt(NBT_TIME_FORMAT));
            DLTime timeOn = new DLTime(nbt.getInt(NBT_TIME_ON), VanillaTimeSystem.INSTANCE);
            DLTime timeOff = new DLTime(nbt.getInt(NBT_TIME_OFF), VanillaTimeSystem.INSTANCE);
            list.add(TextUtils.translate(keyTurnOn, timeOn.format(format.getFormat(), TimeContext.INGAME)));
            list.add(TextUtils.translate(keyTurnOff, timeOff.format(format.getFormat(), TimeContext.INGAME)));
        } else {
            list.add(textEmpty);
        }        
    }
    
    @Override
    public boolean isFoil(ItemStack pStack) {
        return doesContainValidLinkData(pStack) != null || super.isFoil(pStack);
    }

    public static CompoundTag doesContainValidLinkData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_TIME_ON) && tag.contains(NBT_TIME_OFF) && tag.contains(NBT_TIME_FORMAT) ? tag : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        CompoundTag nbt = null;
        ItemStack stack = pContext.getItemInHand();

        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        Player player = pContext.getPlayer();

        if (!player.isShiftKeyDown() && state.getBlock() instanceof StreetLampBaseBlock && level.getBlockEntity(pos) instanceof StreetLampBlockEntity blockEntity) {
            if (!level.isClientSide) {
                if ((nbt = doesContainValidLinkData(stack)) != null) {
                    if (nbt.getInt(NBT_TIME_ON) == nbt.getInt(NBT_TIME_OFF)) {
                        player.displayClientMessage(textErrorTimeEqual, false);                        
                        return InteractionResult.FAIL;
                    }
                    blockEntity.setOnTime(nbt.getInt(NBT_TIME_ON));
                    blockEntity.setOffTime(nbt.getInt(NBT_TIME_OFF));
                    player.displayClientMessage(textApply, true);
                    DLUtils.giveAdvancement((ServerPlayer)player, TrafficCraft.MOD_ID, "street_lamp_config", "requirement");
                } else {
                    blockEntity.setOnTime(-1);
                    blockEntity.setOffTime(-1);
                    player.displayClientMessage(textRemove, true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(pContext);
    }
}