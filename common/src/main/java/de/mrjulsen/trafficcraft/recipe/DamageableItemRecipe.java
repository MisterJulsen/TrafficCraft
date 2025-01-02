package de.mrjulsen.trafficcraft.recipe;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageableItemRecipe extends ShapelessRecipe {

    public DamageableItemRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
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
    
    public static class Serializer implements RecipeSerializer<DamageableItemRecipe> {
        public Serializer() {
        }

        @SuppressWarnings("deprecation")
        public DamageableItemRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String string = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory craftingBookCategory = (CraftingBookCategory) CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", (String) null), CraftingBookCategory.MISC);
            NonNullList<Ingredient> nonNullList = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (nonNullList.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (nonNullList.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
                return new DamageableItemRecipe(recipeId, string, craftingBookCategory, itemStack, nonNullList);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray ingredientArray) {
            NonNullList<Ingredient> nonNullList = NonNullList.create();

            for (int i = 0; i < ingredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i), false);
                if (!ingredient.isEmpty()) {
                    nonNullList.add(ingredient);
                }
            }

            return nonNullList;
        }

        public DamageableItemRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String string = buffer.readUtf();
            CraftingBookCategory craftingBookCategory = (CraftingBookCategory) buffer.readEnum(CraftingBookCategory.class);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);

            for (int j = 0; j < nonNullList.size(); ++j) {
                nonNullList.set(j, Ingredient.fromNetwork(buffer));
            }

            ItemStack itemStack = buffer.readItem();
            return new DamageableItemRecipe(recipeId, string, craftingBookCategory, itemStack, nonNullList);
        }

        public void toNetwork(FriendlyByteBuf buffer, DamageableItemRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());
            Iterator<Ingredient> var3 = recipe.getIngredients().iterator();

            while (var3.hasNext()) {
                Ingredient ingredient = (Ingredient) var3.next();
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.getResultItem(null));
        }
    }
}
