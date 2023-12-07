package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.block.PaintBucketBlock;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class BrushItem extends Item
{
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
            if (player.isShiftKeyDown()) {
                ClientWrapper.showPaintBrushScreen(nbt.getInt("pattern"), nbt.getInt("paint"), nbt.getInt("color"), nbt.getFloat("scroll"));
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        
        if(stack.hasTag())
        {
            String color = GuiUtils.translate(PaintColor.byId(stack.getTag().getInt("color")).getTranslatableString()).getString();
            char colorCode = PaintColor.byId(stack.getTag().getInt("color")).getColorCode();

            if (stack.getTag().getInt("paint") == 0) {                
                color = GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.color_empty").getString();
                colorCode = 'r';
            }

            list.add(GuiUtils.text(GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.pattern").getString() + stack.getTag().getInt("pattern")));
            list.add(GuiUtils.text(GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.color").getString() + "§" + colorCode + color));
            list.add(GuiUtils.text(GuiUtils.translate("item.trafficcraft.paint_brush.tooltip.paint").getString() + (int)(100.0f / Constants.MAX_PAINT * stack.getTag().getInt("paint")) + " %"));
        }
        
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if(checkNbt(stack).getInt("paint") > 0)
            return true;
        else
            return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (checkNbt(stack).getInt("paint") * 13) / Constants.MAX_PAINT;
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
            nbt.putInt("paint", 0);
            nbt.putInt("pattern", 0);
            nbt.putInt("color", 0xFFFFFFFF);
            nbt.putFloat("scroll", 0.0f);
        }

        return nbt;
    }

    public int getPaintAmount() {
        return this.paintAmount;
    }

    public static PaintColor getColor(ItemStack stack) {
        return PaintColor.byId(checkNbt(stack).getInt("color"));
    }

    public static int getPatternId(ItemStack stack) {
        return checkNbt(stack).getInt("pattern");
    }

    public static int getPaint(ItemStack stack) {
        return checkNbt(stack).getInt("paint");
    }

    public int getMaxPaint() {
        return Constants.MAX_PAINT;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        CompoundTag nbt = checkNbt(pContext.getItemInHand());
        pContext.getItemInHand().setTag(nbt);

        if (nbt.getInt("paint") <= 0) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        Player player = pContext.getPlayer();

        if (!level.isClientSide) {
            if (state.getBlock() instanceof PaintBucketBlock) {
                level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
                return InteractionResult.SUCCESS;
            } else {
                if (state.getBlock() instanceof IPaintableBlock block) {
                    if (level.getBlockEntity(pos) instanceof IColorBlockEntity blockEntity && blockEntity.getColor() == PaintColor.byId(nbt.getInt("color"))) { 
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
        }
        return InteractionResult.FAIL;
    }

    private void removePaint(Player player, CompoundTag nbt) {
        if (!player.isCreative()) {                    
            nbt.putInt("paint", nbt.getInt("paint") - 1);
        }
    }
}