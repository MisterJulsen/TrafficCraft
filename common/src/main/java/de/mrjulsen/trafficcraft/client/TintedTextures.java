package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TintedTextures {

    public static class TintedBlock implements BlockColor {
        @Override
        public int getColor(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos, int pTintIndex) {
            
            if (pState.getBlock() instanceof IPaintableBlock block) {
                if (pLevel == null) {
                    return block.getDefaultColor().getAsARGB();
                }

                if (pLevel.getBlockEntity(pPos) instanceof IColorBlockEntity blockEntity) {
                    PaintColor c = blockEntity.getColor();
                    return c == PaintColor.NONE ? block.getDefaultColor().getAsARGB() : c.getTextureColor().getAsARGB();
                }
            }
                        
            return 0;
        }
    }

    public static class TintedItem implements ItemColor {
        @Override
        public int getColor(ItemStack pStack, int pTintIndex) {

            if (pStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof IPaintableBlock coloredBlock) {
                    return coloredBlock.getDefaultColor().getAsARGB();
                }
            } else if (pStack.getItem() instanceof BrushItem item) {                
                if (pTintIndex == 1) {
                    return (0xFF << 24) | (PaintColor.getByIndex(item.getComponent(pStack).colorId()).getTextureColor().getAsARGB() & 0x00FFFFFF);
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
