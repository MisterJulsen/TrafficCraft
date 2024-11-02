package de.mrjulsen.trafficcraft.recipe;

import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageableItemRecipe extends ShapelessRecipe {

    public DamageableItemRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItems.DAMAGEABLE_ITEM_RECIPE.get();
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = container.getItem(i);
            Item item = stack.getItem();
            if (item instanceof IDamageableCraftingItem) {
                int newDamage = stack.getDamageValue() + 1;
                if (newDamage < stack.getMaxDamage()) {
                    stack = stack.copy();
                    stack.setDamageValue(newDamage);
                    defaultedList.set(i, stack);
                }
            } else if (item.hasCraftingRemainingItem()) {
                defaultedList.set(i, new ItemStack(item.getCraftingRemainingItem()));
            }
        }

        return defaultedList;
    }
}
