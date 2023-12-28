package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.block.PaintBucketBlock;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class BrushItem extends Item {

    public static final String NBT_PATTERN = "pattern";
    public static final String NBT_PAINT = "paint";
    public static final String NBT_COLOR = "color";

    private int paintAmount = 0;

    public BrushItem(Properties properties, int paintAmount) {
        super(properties.stacksTo(1));
        this.paintAmount = paintAmount;        
    }

    
    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if (player.isCreative()) {
            if (state.getBlock() instanceof IPaintableBlock block) {  
                block.onRemoveColor(state, worldIn, pos, player);
                return false;
            }
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag nbt = checkNbt(stack);
        stack.setTag(nbt);

        if (level.isClientSide) {
            ClientWrapper.showPaintBrushScreen(nbt.getInt(NBT_PATTERN), nbt.getInt(NBT_PAINT), PaintColor.byId(nbt.getInt(NBT_COLOR)));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        
        if (stack.hasTag()) {
            PaintColor paintColor = PaintColor.byId(stack.getTag().getInt(NBT_COLOR));
            String color = Utils.translate(paintColor.getTranslatableString()).getString();
    
            list.add(Utils.translate("item.trafficcraft.paint_brush.tooltip.pattern", "§f" + stack.getTag().getInt(NBT_PATTERN)).withStyle(ChatFormatting.GRAY));            
            if (stack.getTag().getInt(NBT_PAINT) == 0) {
                list.add(Utils.translate("item.trafficcraft.paint_brush.tooltip.color", Utils.translate("item.trafficcraft.paint_brush.tooltip.color_empty")).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(Utils.translate("item.trafficcraft.paint_brush.tooltip.color", Utils.text(color).withStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE).withColor(paintColor.getTextureColor()))).withStyle(ChatFormatting.GRAY));
            }
            list.add(Utils.translate("item.trafficcraft.paint_brush.tooltip.paint", "§f" + (int)(100.0f / Constants.MAX_PAINT * stack.getTag().getInt(NBT_PAINT))).withStyle(ChatFormatting.GRAY));
        }
        
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if(checkNbt(stack).getInt(NBT_PAINT) > 0)
            return true;
        else
            return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (checkNbt(stack).getInt(NBT_PAINT) * 13) / Constants.MAX_PAINT;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return getColor(pStack).getTextureColor();
    }

    public static CompoundTag checkNbt(ItemStack stack) {
        CompoundTag nbt;

        if (stack.hasTag()) {
            nbt = stack.getTag();
        } else {
            nbt = new CompoundTag();
            nbt.putInt(NBT_PAINT, 0);
            nbt.putInt(NBT_PATTERN, 0);
            nbt.putInt(NBT_COLOR, 0xFFFFFFFF);
        }

        return nbt;
    }

    public int getPaintAmount() {
        return this.paintAmount;
    }

    public static PaintColor getColor(ItemStack stack) {
        return PaintColor.byId(checkNbt(stack).getInt(NBT_COLOR));
    }

    public static int getPatternId(ItemStack stack) {
        return checkNbt(stack).getInt(NBT_PATTERN);
    }

    public static int getPaint(ItemStack stack) {
        return checkNbt(stack).getInt(NBT_PAINT);
    }

    public int getMaxPaint() {
        return Constants.MAX_PAINT;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        CompoundTag nbt = checkNbt(pContext.getItemInHand());
        pContext.getItemInHand().setTag(nbt);

        if (nbt.getInt(NBT_PAINT) <= 0) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        Player player = pContext.getPlayer();

        if (!level.isClientSide) {
            
        }
        if (state.getBlock() instanceof PaintBucketBlock) {
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.SUCCESS;
            } else {
                if (state.getBlock() instanceof IPaintableBlock block) {
                    if (level.getBlockEntity(pos) instanceof IColorBlockEntity blockEntity && blockEntity.getColor() == PaintColor.byId(nbt.getInt(NBT_COLOR))) { 
                        InteractionResult res = block.update(pContext);
                        if (res == InteractionResult.CONSUME) {
                            this.removePaint(player, nbt);
                            res = InteractionResult.SUCCESS;
                        }
                        return res;
                    }

                    InteractionResult res = block.onSetColor(pContext);
                    if (res == InteractionResult.CONSUME) {
                        this.removePaint(player, nbt);
                        res = InteractionResult.SUCCESS;
                    }
                    return res;
                }
            }
        return InteractionResult.PASS;
    }

    private void removePaint(Player player, CompoundTag nbt) {
        if (!player.isCreative()) {                    
            nbt.putInt(NBT_PAINT, nbt.getInt(NBT_PAINT) - 1);
        }
    }
}