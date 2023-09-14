package de.mrjulsen.trafficcraft.block.colors;

import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
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
            
            if (pState.getBlock() instanceof IPaintableBlock block) {
                if (pLevel == null) {
                    return block.getDefaultColor();
                }

                if (pLevel.getBlockEntity(pPos) instanceof IColorStorageBlockEntity blockEntity) {
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
                if (blockItem.getBlock() instanceof IPaintableBlock coloredBlock) {
                    return coloredBlock.getDefaultColor();
                }
            } else if (pStack.getItem() instanceof BrushItem) {                
                if (pTintIndex == 1) {
                    return BrushItem.getColor(pStack).getTextureColor();
                } else {
                    return 0xFFFFFFFF;
                }
            } else if (pStack.getItem() instanceof ColorPaletteItem) {
                if (pTintIndex == 0) {
                    return 0xFFFFFFFF;
                }
                int color = ColorPaletteItem.getColorAt(pStack, pTintIndex - 1);
                if (color == 0) {
                    color = 0xFFFFFFFF;
                }
                return color;
            }
            return 0;
        }
        
    }
    
}
