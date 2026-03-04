package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.HammerItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import de.mrjulsen.trafficcraft.recipe.DamageableItemRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import dev.architectury.extensions.injected.InjectedItemPropertiesExtension;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.ITEM);


    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench", WrenchItem::new);
    public static final RegistrySupplier<Item> TRAFFIC_LIGHT_LINKER = ITEMS.register("traffic_light_linker", () -> new TrafficLightLinkerItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB).stacksTo(1)));
    public static final RegistrySupplier<Item> BITUMEN = ITEMS.register("raw_bitumen", () -> new Item(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_ROD = ITEMS.register("iron_rod", () -> new Item(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_PLATE = ITEMS.register("iron_plate", () -> new Item(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new BrushItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB),0));
    public static final RegistrySupplier<Item> WOOD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("wood_road_construction_tool", () -> new RoadConstructionTool(Tiers.WOOD, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> STONE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("stone_road_construction_tool", () -> new RoadConstructionTool(Tiers.STONE, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_ROAD_CONSTRUCTION_TOOL = ITEMS.register("iron_road_construction_tool", () -> new RoadConstructionTool(Tiers.IRON, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> GOLD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("gold_road_construction_tool", () -> new RoadConstructionTool(Tiers.GOLD, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> DIAMOND_ROAD_CONSTRUCTION_TOOL = ITEMS.register("diamond_road_construction_tool", () -> new RoadConstructionTool(Tiers.DIAMOND, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> NETHERITE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("netherite_road_construction_tool", () -> new RoadConstructionTool(Tiers.NETHERITE, ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> HAMMER = ITEMS.register("hammer", () -> new HammerItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> STREET_LAMP_CONFIG_CARD = ITEMS.register("street_lamp_config_card", () -> new StreetLampConfigCardItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> COLOR_PALETTE = ITEMS.register("color_palette", () -> new ColorPaletteItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> PATTERN_CATALOGUE = ITEMS.register("pattern_catalogue", () -> new PatternCatalogueItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> CREATIVE_PATTERN_CATALOGUE = ITEMS.register("creative_pattern_catalogue", () -> new CreativePatternCatalogueItem(((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(ModCreativeModeTab.MOD_TAB)));

    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.RECIPE_SERIALIZER);
    public static final RegistrySupplier<RecipeSerializer<?>> DAMAGEABLE_ITEM_RECIPE_SERIALIZER = RECIPES.register("damageable_item_recipe", () -> new DamageableItemRecipe.Serializer());
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.RECIPE_TYPE);
    public static final RegistrySupplier<RecipeType<?>> DAMAGEABLE_ITEM_RECIPE_TYPE = RECIPE_TYPES.register("damageable_recipe_type", () -> RecipeType.register(TrafficCraft.MOD_ID + "_damageable_recipe_type"));

    /*
    public static final RecipeType<DamageableItemRecipe> DAMAGEABLE_RECIPE_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE,
        ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "damageable_item_recipe_type"), new RecipeType<DamageableItemRecipe>() {
            @Override
            public String toString() {
                return "damageable_item_recipe_type";
            }
        });
        */
    

    public static void register() {
        ITEMS.register();
        //RecipeType.register(TrafficCraft.MOD_ID + "_damageable_recipe_type");
        RECIPE_TYPES.register();
        RECIPES.register();
    }

}
