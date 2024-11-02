package de.mrjulsen.trafficcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageableItemRecipe extends ShapelessRecipe {

    public DamageableItemRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModItems.DAMAGEABLE_ITEM_RECIPE_SERIALIZER.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = input.getItem(i);
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
		private static final MapCodec<DamageableItemRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(shapelessRecipe -> shapelessRecipe.getGroup()),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(shapelessRecipe -> shapelessRecipe.category()),
						ItemStack.STRICT_CODEC.fieldOf("result").forGetter(shapelessRecipe -> shapelessRecipe.getResultItem(null)),
						Ingredient.CODEC_NONEMPTY
							.listOf()
							.fieldOf("ingredients")
							.flatXmap(
								list -> {
									Ingredient[] ingredients = (Ingredient[])list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
									if (ingredients.length == 0) {
										return DataResult.error(() -> "No ingredients for shapeless recipe");
									} else {
										return ingredients.length > 9
											? DataResult.error(() -> "Too many ingredients for shapeless recipe")
											: DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
									}
								},
								DataResult::success
							)
							.forGetter(shapelessRecipe -> shapelessRecipe.getIngredients())
					)
					.apply(instance, DamageableItemRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, DamageableItemRecipe> STREAM_CODEC = StreamCodec.of(
			DamageableItemRecipe.Serializer::toNetwork, DamageableItemRecipe.Serializer::fromNetwork
		);

		@Override
		public MapCodec<DamageableItemRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, DamageableItemRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static DamageableItemRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			String string = buffer.readUtf();
			CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
			int i = buffer.readVarInt();
			NonNullList<Ingredient> nonNullList = NonNullList.withSize(i, Ingredient.EMPTY);
			nonNullList.replaceAll(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
			ItemStack itemStack = ItemStack.STREAM_CODEC.decode(buffer);
			return new DamageableItemRecipe(string, craftingBookCategory, itemStack, nonNullList);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, DamageableItemRecipe recipe) {
			buffer.writeUtf(recipe.getGroup());
			buffer.writeEnum(recipe.category());
			buffer.writeVarInt(recipe.getIngredients().size());

			for (Ingredient ingredient : recipe.getIngredients()) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
			}

			ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
		}
	}
}
