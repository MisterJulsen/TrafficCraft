package de.mrjulsen.legacydragonlib.internal.content;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public class DragonLibSampleBlock extends Block {

    public DragonLibSampleBlock(Properties properties) {
        super(properties);
    }

    public static class DragonItem extends BlockItem {

        public DragonItem(Block pBlock, Properties pProperties) {
            super(pBlock, pProperties.rarity(Rarity.EPIC));            
        }
    }
    
}
