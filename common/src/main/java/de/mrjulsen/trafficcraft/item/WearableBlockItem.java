package de.mrjulsen.trafficcraft.item;

import org.jetbrains.annotations.Nullable;

import dev.architectury.extensions.ItemExtension;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;

public class WearableBlockItem extends BlockItem implements Equipable, ItemExtension {

    public WearableBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }
    
    @Override
    public @Nullable EquipmentSlot getCustomEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
