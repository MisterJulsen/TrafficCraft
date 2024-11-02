package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.components.BrushComponent;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import de.mrjulsen.trafficcraft.block.PaintBucketBlock;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
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

public class BrushItem extends Item implements IUseDataComponent<BrushComponent> {
    
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

        if (level.isClientSide) {
            BrushComponent comp = getComponent(stack);
            ClientWrapper.showPaintBrushScreen(comp.patternId(), comp.paintAmount(), PaintColor.getByIndex(comp.colorId()));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        if (hasComponent(stack)) {
            BrushComponent comp = getComponent(stack);
            PaintColor paintColor = PaintColor.getByIndex(comp.colorId());
            String color = TextUtils.translate(paintColor.getValueTranslationKey(TrafficCraft.MOD_ID)).getString();
    
            tooltipComponents.add(TextUtils.translate("item.trafficcraft.paint_brush.tooltip.pattern", "§f" + comp.patternId()).withStyle(ChatFormatting.GRAY));            
            if (comp.paintAmount() == 0) {
                tooltipComponents.add(TextUtils.translate("item.trafficcraft.paint_brush.tooltip.color", TextUtils.translate("item.trafficcraft.paint_brush.tooltip.color_empty")).withStyle(ChatFormatting.GRAY));
            } else {
                tooltipComponents.add(TextUtils.translate("item.trafficcraft.paint_brush.tooltip.color", TextUtils.text(color).withStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE).withColor(paintColor.getTextureColor()))).withStyle(ChatFormatting.GRAY));
            }
            tooltipComponents.add(TextUtils.translate("item.trafficcraft.paint_brush.tooltip.paint", "§f" + (int)(100.0f / Constants.MAX_PAINT * comp.paintAmount())).withStyle(ChatFormatting.GRAY));
        }
        
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (hasComponent(stack) && getComponent(stack).paintAmount() > 0)
            return true;
        else
            return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return hasComponent(stack) ? (getComponent(stack).paintAmount() * 13) / Constants.MAX_PAINT : 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return hasComponent(pStack) ? getColor(pStack).getTextureColor() : 0;
    }

    public int getPaintAmount() {
        return this.paintAmount;
    }

    public PaintColor getColor(ItemStack stack) {
        return PaintColor.getByIndex(getComponent(stack).colorId());
    }

    public int getPatternId(ItemStack stack) {
        return getComponent(stack).patternId();
    }

    public int getPaint(ItemStack stack) {
        return getComponent(stack).paintAmount();
    }

    public int getMaxPaint() {
        return Constants.MAX_PAINT;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        ItemStack stack = pContext.getItemInHand();

        if (!hasComponent(stack)) {
            return InteractionResult.FAIL;
        }

        BrushComponent comp = getComponent(stack);

        if (comp.paintAmount() <= 0) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        Player player = pContext.getPlayer();

        if (state.getBlock() instanceof PaintBucketBlock) {
            level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
            return InteractionResult.SUCCESS;
        } else {
            if (state.getBlock() instanceof IPaintableBlock block) {
                if (level.getBlockEntity(pos) instanceof IColorBlockEntity blockEntity && blockEntity.getColor() == PaintColor.getByIndex(comp.colorId())) { 
                    InteractionResult res = block.update(pContext);
                    if (res == InteractionResult.CONSUME) {
                        this.removePaint(player, stack, comp);
                        res = InteractionResult.SUCCESS;
                    }
                    return res;
                }

                InteractionResult res = block.onSetColor(pContext);
                if (res == InteractionResult.CONSUME) {
                    this.removePaint(player, stack, comp);
                    res = InteractionResult.SUCCESS;
                }
                return res;
            }
        }
        return InteractionResult.PASS;
    }

    private void removePaint(Player player, ItemStack stack, BrushComponent component) {
        if (!player.isCreative()) {
            stack.set(ModDataComponents.BRUSH_COMPONENT.get(), new BrushComponent(
                component.patternId(),
                component.paintAmount() - 1,
                component.colorId()
            ));
        }
    }


    @Override
    public DataComponentType<BrushComponent> getComponentType() {
        return ModDataComponents.BRUSH_COMPONENT.get();
    }


    @Override
    public BrushComponent emptyComponent() {
        return BrushComponent.empty();
    }
}