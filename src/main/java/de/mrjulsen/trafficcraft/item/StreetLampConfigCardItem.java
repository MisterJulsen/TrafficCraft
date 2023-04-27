package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.TimeFormat;
import de.mrjulsen.trafficcraft.item.client.StreetLampConfigCardClient;
import de.mrjulsen.trafficcraft.util.TimeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.BlockState;

public class StreetLampConfigCardItem extends Item
{

    public StreetLampConfigCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    
    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if (player.isCreative()) {
            if (state.getBlock() instanceof StreetLampBaseBlock block) {  
                return false;
            }
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        

        if (level.isClientSide) {
            if (player.isShiftKeyDown()) {
                stack.setTag(new CompoundTag());
                player.displayClientMessage(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.use.clear"), true);
            } else {
                int turnOn = 18500;
                int turnOff = 5500;
                int timeFormat = 0;
                CompoundTag nbt = null;
                if ((nbt = doesContainValidLinkData(stack)) != null) {
                    turnOn = nbt.getInt("turnOnTime");
                    turnOff = nbt.getInt("turnOffTime");
                    timeFormat = nbt.getInt("timeFormat");
                }

                StreetLampConfigCardClient.showGui(turnOn, turnOff, TimeFormat.getFormatByIndex(timeFormat));
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        
        CompoundTag nbt = null;
        if ((nbt = doesContainValidLinkData(stack)) != null) {
            list.add(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.tooltip.turn_on_time", TimeUtils.parseTime(nbt.getInt("turnOnTime"), TimeFormat.getFormatByIndex(nbt.getInt("timeFormat")))));
            list.add(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.tooltip.turn_off_time", TimeUtils.parseTime(nbt.getInt("turnOffTime"), TimeFormat.getFormatByIndex(nbt.getInt("timeFormat")))));            
        } else {
            list.add(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.tooltip.empty"));
        }        
    }
    
    @Override
    public boolean isFoil(ItemStack pStack) {
        return doesContainValidLinkData(pStack) != null || super.isFoil(pStack);
    }

    public static CompoundTag doesContainValidLinkData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("turnOnTime") && tag.contains("turnOffTime") && tag.contains("timeFormat") ? tag : null;
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
                    if (nbt.getInt("turnOnTime") == nbt.getInt("turnOffTime")) {
                        player.displayClientMessage(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.use.error_same_time"), false);                        
                        return InteractionResult.FAIL;
                    }
                    blockEntity.setOnTime(TimeUtils.shiftTimeToMinecraftTicks(nbt.getInt("turnOnTime")));
                    blockEntity.setOffTime(TimeUtils.shiftTimeToMinecraftTicks(nbt.getInt("turnOffTime")));
                    player.displayClientMessage(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.use.set"), true);
                } else {
                    blockEntity.setOnTime(-1);
                    blockEntity.setOffTime(-1);
                    player.displayClientMessage(new TranslatableComponent("item.trafficcraft.street_lamp_config_card.use.unset"), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}