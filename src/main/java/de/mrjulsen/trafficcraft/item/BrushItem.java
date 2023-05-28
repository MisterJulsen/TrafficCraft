package de.mrjulsen.trafficcraft.item;

import java.util.List;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltBlock;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltSlope;
import de.mrjulsen.trafficcraft.block.StreetSignBlock;
import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.AsphaltBlock;
import de.mrjulsen.trafficcraft.block.AsphaltSlope;
import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.block.PaintBucketBlock;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.ColorableBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.item.client.BrushClient;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
            if (state.getBlock() instanceof ColorableBlock block) {  
                block.onRemoveColor(state, worldIn, pos, player);
                return false;
            } else if (state.getBlock() instanceof StreetSignBlock block) {  
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
                BrushClient.showGui(nbt.getInt("pattern"), nbt.getInt("paint"), nbt.getInt("color"), nbt.getFloat("scroll"));
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, player, list, flag);
        
        if(stack.hasTag())
        {
            String color = new TranslatableComponent(PaintColor.byId(stack.getTag().getInt("color")).getTranslatableString()).getString();
            char colorCode = PaintColor.byId(stack.getTag().getInt("color")).getColorCode();

            if (stack.getTag().getInt("paint") == 0) {                
                color = new TranslatableComponent("item.trafficcraft.paint_brush.tooltip.color_empty").getString();
                colorCode = 'r';
            }

            list.add(new TextComponent(new TranslatableComponent("item.trafficcraft.paint_brush.tooltip.pattern").getString() + stack.getTag().getInt("pattern")));
            list.add(new TextComponent(new TranslatableComponent("item.trafficcraft.paint_brush.tooltip.color").getString() + "§" + colorCode + color));
            list.add(new TextComponent(new TranslatableComponent("item.trafficcraft.paint_brush.tooltip.paint").getString() + (int)(100.0f / Constants.MAX_PAINT * stack.getTag().getInt("paint")) + " %"));
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

        if (state.getBlock() instanceof IPaintableBlock) {
            
            if (!level.isClientSide) {
                if (state.getBlock() instanceof PaintBucketBlock) {
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 0.8F, 1.0F);
                    return InteractionResult.SUCCESS;
                } else if (state.getBlock() instanceof RoadBlock road && level.getBlockEntity(pos) instanceof ColoredBlockEntity blockEntity) {
    
                    String id = "";
                    if (state.getBlock() instanceof PaintedAsphaltBlock)
                        id = road.getDefaultRoadType().getRoadType() + "_pattern_" + nbt.getInt("pattern");
                    else if (state.getBlock() instanceof PaintedAsphaltSlope)
                        id = road.getDefaultRoadType().getRoadType() + "_slope_pattern_" + nbt.getInt("pattern");
                        
                    if (!ModBlocks.ROAD_BLOCKS.containsKey(id)) {
                        return InteractionResult.FAIL;
                    }
    
                    if (state.getBlock() == ModBlocks.ROAD_BLOCKS.get(id).get() && blockEntity.getColor() == PaintColor.byId(nbt.getInt("color"))) {
                        level.setBlockAndUpdate(pos, state.setValue(RoadBlock.FACING, state.getValue(RoadBlock.FACING).getClockWise(Axis.Y)));
                        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                        return InteractionResult.SUCCESS;
                    } else {
                        BlockState newState = ModBlocks.ROAD_BLOCKS.get(id).get().defaultBlockState().setValue(RoadBlock.FACING, player.getDirection());
                        if (state.getBlock() instanceof PaintedAsphaltSlope) {
                            newState = newState.setValue(PaintedAsphaltSlope.LAYERS, state.getValue(PaintedAsphaltSlope.LAYERS));
                        }
    
                        level.setBlockAndUpdate(pos, newState);
                        road.onSetColor(level, pos, PaintColor.byId(nbt.getInt("color")));
    
                        this.removePaint(player, nbt);
                        return InteractionResult.SUCCESS;
                    }
                    
                } else if (state.getBlock() instanceof AsphaltBlock block) {
    
                    String id = "";

                    if (state.getBlock() instanceof AsphaltSlope)
                        id = block.getDefaultRoadType().getRoadType() + "_slope_pattern_" + nbt.getInt("pattern");
                    else if (state.getBlock() instanceof AsphaltBlock)
                        id = block.getDefaultRoadType().getRoadType() + "_pattern_" + nbt.getInt("pattern");
                        
                    if (!ModBlocks.ROAD_BLOCKS.containsKey(id) || !(ModBlocks.ROAD_BLOCKS.get(id).get() instanceof RoadBlock)) {
                        return InteractionResult.FAIL;
                    }

                    RoadBlock road = (RoadBlock)ModBlocks.ROAD_BLOCKS.get(id).get();
                    BlockState newState = road.defaultBlockState().setValue(RoadBlock.FACING, player.getDirection());

                    if (state.getBlock() instanceof AsphaltSlope) {
                        newState = newState.setValue(PaintedAsphaltSlope.LAYERS, state.getValue(AsphaltSlope.LAYERS));
                    }

                    level.setBlockAndUpdate(pos, newState);
                    road.onSetColor(level, pos, PaintColor.byId(nbt.getInt("color")));

                    this.removePaint(player, nbt);
                    return InteractionResult.SUCCESS;
                    
                } else if (state.getBlock() instanceof StreetSignBlock block && level.getBlockEntity(pos) instanceof StreetSignBlockEntity blockEntity) {
                    if (blockEntity.getColor() != PaintColor.byId(nbt.getInt("color"))) { 
                        block.onSetColor(level, pos, PaintColor.byId(nbt.getInt("color")));    
                        this.removePaint(player, nbt);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (state.getBlock() instanceof ColorableBlock block && level.getBlockEntity(pos) instanceof ColoredBlockEntity blockEntity) {
                        if (blockEntity.getColor() != PaintColor.byId(nbt.getInt("color"))) { 
                            block.onSetColor(level, pos, PaintColor.byId(nbt.getInt("color")));
        
                            this.removePaint(player, nbt);
                            return InteractionResult.SUCCESS;
                        }
                    }
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