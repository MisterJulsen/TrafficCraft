package de.mrjulsen.trafficcraft.block.colors;

import de.mrjulsen.trafficcraft.block.HouseNumberSignBlock;
import de.mrjulsen.trafficcraft.block.StreetSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.HouseNumberSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.ColorableBlock;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TintedTextures{

    public static class BasicBlockTint implements BlockColor {
        @Override
        public int getColor(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos, int pTintIndex) {
            
            if (pState.getBlock() instanceof ColorableBlock block) {
                if (pLevel.getBlockEntity(pPos) instanceof ColoredBlockEntity blockEntity) {
                    PaintColor c = blockEntity.getColor();
                    return c == PaintColor.NONE ? block.getDefaultColor() : c.getTextureColor();
                }
            } else if (pState.getBlock() instanceof StreetSignBlock block) {    
                if (pLevel == null) {
                    return block.getDefaultColor();
                }

                if (pLevel.getBlockEntity(pPos) instanceof StreetSignBlockEntity blockEntity) {                    
                    PaintColor c = blockEntity.getColor();
                    return c == PaintColor.NONE ? block.getDefaultColor() : c.getTextureColor();
                }
            } else if (pState.getBlock() instanceof HouseNumberSignBlock block) {    
                if (pLevel == null) {
                    return block.getDefaultColor();
                }

                if (pLevel.getBlockEntity(pPos) instanceof HouseNumberSignBlockEntity blockEntity) {                    
                    PaintColor c = blockEntity.getColor();
                    return c == PaintColor.NONE ? block.getDefaultColor() : c.getTextureColor();
                }
            } else if (pState.getBlock() instanceof TrafficLightBlock block) {    
                if (pLevel == null) {
                    return block.getDefaultColor();
                }

                if (pLevel.getBlockEntity(pPos) instanceof TrafficLightBlockEntity blockEntity) {                    
                    PaintColor c = blockEntity.getColor();
                    return c == PaintColor.NONE ? block.getDefaultColor() : c.getTextureColor();
                }
            }
                        
            return 0;
        }
    }

    public static class BasicItemTint implements ItemColor {

        @Override
        public int getColor(ItemStack pStack, int pTintIndex) {

            if (pStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof ColorableBlock coloredBlock) {
                    return coloredBlock.getDefaultColor();
                }
            } else if (pStack.getItem() instanceof BrushItem) {                
                if (pTintIndex == 1) {
                    return BrushItem.getColor(pStack).getTextureColor();
                } else {
                    return 0xFFFFFFFF;
                }
            }
            return 0;
        }
        
    }
    
}
