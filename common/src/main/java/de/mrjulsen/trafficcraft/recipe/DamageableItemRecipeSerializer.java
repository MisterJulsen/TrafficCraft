package de.mrjulsen.trafficcraft.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageableItemRecipeSerializer extends ShapelessRecipe.Serializer {

    public static final DamageableItemRecipeSerializer INSTANCE = new DamageableItemRecipeSerializer();

    @SuppressWarnings("deprecation")
    @Override
    public ShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String string = GsonHelper.getAsString(json, "group", "");
        CraftingBookCategory craftingBookCategory = (CraftingBookCategory)CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", (String)null), CraftingBookCategory.MISC);
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

        for(int i = 0; i < ingredientArray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
            if (!ingredient.isEmpty()) {
                nonNullList.add(ingredient);
            }
        }

        return nonNullList;
    }

    @Override
    public ShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String string = buffer.readUtf();
        int i = buffer.readVarInt();
        CraftingBookCategory craftingBookCategory = (CraftingBookCategory)buffer.readEnum(CraftingBookCategory.class);
        NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);
  
        for(int j = 0; j < nonNullList.size(); ++j) {
           nonNullList.set(j, Ingredient.fromNetwork(buffer));
        }
  
        ItemStack itemStack = buffer.readItem();
        return new DamageableItemRecipe(recipeId, string, craftingBookCategory, itemStack, nonNullList);
    }   

}
