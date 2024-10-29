package de.mrjulsen.trafficcraft.recipe;

import java.util.Iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DamageableItemRecipeSerializer implements RecipeSerializer<DamageableItemRecipe> {

    public static final DamageableItemRecipeSerializer INSTANCE = new DamageableItemRecipeSerializer();
   
    private static final Codec<DamageableItemRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((shapelessRecipe) -> {
            return shapelessRecipe.getGroup();
        }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((shapelessRecipe) -> {
            return shapelessRecipe.category();
        }), ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((shapelessRecipe) -> {
            return shapelessRecipe.getResultItem(null);
        }), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((list) -> {
            Ingredient[] ingredients = (Ingredient[])list.stream().filter((ingredient) -> {
                return !ingredient.isEmpty();
            }).toArray((i) -> {
                return new Ingredient[i];
            });
            if (ingredients.length == 0) {
                return DataResult.error(() -> {
                return "No ingredients for shapeless recipe";
                });
            } else {
                return ingredients.length > 9 ? DataResult.error(() -> {
                return "Too many ingredients for shapeless recipe";
                }) : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
            }
        }, DataResult::success).forGetter((shapelessRecipe) -> {
            return shapelessRecipe.getIngredients();
        })).apply(instance, (a, b, c, d) -> new DamageableItemRecipe(a, b, c, d));
    });

    public Codec<DamageableItemRecipe> codec() {
        return CODEC;
    }

    @Override
    public DamageableItemRecipe fromNetwork(FriendlyByteBuf buffer) {
        String string = buffer.readUtf();
        CraftingBookCategory craftingBookCategory = (CraftingBookCategory)buffer.readEnum(CraftingBookCategory.class);
        int i = buffer.readVarInt();
        NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);

        for (int j = 0; j < nonNullList.size(); ++j) {
            nonNullList.set(j, Ingredient.fromNetwork(buffer));
        }

        ItemStack itemStack = buffer.readItem();
        return new DamageableItemRecipe(string, craftingBookCategory, itemStack, nonNullList);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, DamageableItemRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        buffer.writeEnum(recipe.category());
        buffer.writeVarInt(recipe.getIngredients().size());
        Iterator<Ingredient> var3 = recipe.getIngredients().iterator();
    
        while(var3.hasNext()) {
            Ingredient ingredient = (Ingredient)var3.next();
            ingredient.toNetwork(buffer);
        }
    
        buffer.writeItem(recipe.getResultItem(null));
    } 

}
