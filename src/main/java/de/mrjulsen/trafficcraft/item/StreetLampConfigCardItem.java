package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.mcdragonlib.utils.TimeUtils.TimeFormat;
import de.mrjulsen.mcdragonlib.utils.TimeUtils;
import de.mrjulsen.mcdragonlib.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

public class StreetLampConfigCardItem extends Item
{

    private static final String NBT_TIME_ON = "turnOnTime";
    private static final String NBT_TIME_OFF = "turnOffTime";
    private static final String NBT_TIME_FORMAT = "timeFormat";

    private static final Component textEmpty = Utils.translate("item.trafficcraft.street_lamp_config_card.tooltip.empty").withStyle(ChatFormatting.GRAY);
    private static final Component textClear = Utils.translate("item.trafficcraft.street_lamp_config_card.use.clear");
    private static final Component textErrorTimeEqual = Utils.translate("item.trafficcraft.street_lamp_config_card.use.error_same_time").withStyle(ChatFormatting.RED);
    private static final Component textApply = Utils.translate("item.trafficcraft.street_lamp_config_card.use.set").withStyle(ChatFormatting.GREEN);
    private static final Component textRemove = Utils.translate("item.trafficcraft.street_lamp_config_card.use.unset").withStyle(ChatFormatting.RED);
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
                int turnOn = 18500;
                int turnOff = 5500;
                int timeFormat = 0;
                if ((nbt = doesContainValidLinkData(stack)) != null) {
                    turnOn = nbt.getInt(NBT_TIME_ON);
                    turnOff = nbt.getInt(NBT_TIME_OFF);
                }
                timeFormat = stack.getOrCreateTag().getInt(NBT_TIME_FORMAT);

                ClientWrapper.showStreetLampScheduleScreen(turnOn, turnOff, TimeFormat.getFormatByIndex(timeFormat));
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
            list.add(Utils.translate(keyTurnOn, TimeUtils.parseTime(nbt.getInt(NBT_TIME_ON), TimeFormat.getFormatByIndex(nbt.getInt(NBT_TIME_FORMAT)))));
            list.add(Utils.translate(keyTurnOff, TimeUtils.parseTime(nbt.getInt(NBT_TIME_OFF), TimeFormat.getFormatByIndex(nbt.getInt(NBT_TIME_FORMAT)))));            
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

        if (!player.isShiftKeyDown() && state.getBlock() instanceof StreetLampBaseBlock block && level.getBlockEntity(pos) instanceof StreetLampBlockEntity blockEntity) {
            if (!level.isClientSide) {
                if ((nbt = doesContainValidLinkData(stack)) != null) {
                    if (nbt.getInt(NBT_TIME_ON) == nbt.getInt(NBT_TIME_OFF)) {
                        player.displayClientMessage(textErrorTimeEqual, false);                        
                        return InteractionResult.FAIL;
                    }
                    blockEntity.setOnTime(TimeUtils.shiftDayTimeToMinecraftTicks(nbt.getInt(NBT_TIME_ON)));
                    blockEntity.setOffTime(TimeUtils.shiftDayTimeToMinecraftTicks(nbt.getInt(NBT_TIME_OFF)));
                    player.displayClientMessage(textApply, true);
                    Utils.giveAdvancement((ServerPlayer)player, ModMain.MOD_ID, "street_lamp_config", "requirement");
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