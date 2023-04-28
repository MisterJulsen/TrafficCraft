package de.mrjulsen.trafficcraft.block;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class DragonBlock extends Block {

    public DragonBlock(Properties properties) {
        super(properties);
    }

    public static class DragonItem extends BlockItem {

        public DragonItem(Block pBlock, Properties pProperties) {
            super(pBlock, pProperties.rarity(Rarity.EPIC));            
        }

        @Override
        public void appendHoverText(ItemStack stack, Level player, List<Component> list, TooltipFlag flag) {
            super.appendHoverText(stack, player, list, flag);            
            list.add(new TranslatableComponent("core.trafficcraft.credits.line1"));
            list.add(new TranslatableComponent("core.trafficcraft.credits.line2"));
        }
        
    }
    
}
