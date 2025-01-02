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
import de.mrjulsen.trafficcraft.recipe.DamageableItemRecipeSerializer;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(TrafficCraft.MOD_ID, Registry.ITEM_REGISTRY);


    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench", () -> new WrenchItem());
    public static final RegistrySupplier<Item> TRAFFIC_LIGHT_LINKER = ITEMS.register("traffic_light_linker", () -> new TrafficLightLinkerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB).stacksTo(1)));
    public static final RegistrySupplier<Item> BITUMEN = ITEMS.register("raw_bitumen", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_ROD = ITEMS.register("iron_rod", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_PLATE = ITEMS.register("iron_plate", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new BrushItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB),0));
    public static final RegistrySupplier<Item> WOOD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("wood_road_construction_tool", () -> new RoadConstructionTool(Tiers.WOOD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> STONE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("stone_road_construction_tool", () -> new RoadConstructionTool(Tiers.STONE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> IRON_ROAD_CONSTRUCTION_TOOL = ITEMS.register("iron_road_construction_tool", () -> new RoadConstructionTool(Tiers.IRON, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> GOLD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("gold_road_construction_tool", () -> new RoadConstructionTool(Tiers.GOLD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> DIAMOND_ROAD_CONSTRUCTION_TOOL = ITEMS.register("diamond_road_construction_tool", () -> new RoadConstructionTool(Tiers.DIAMOND, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> NETHERITE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("netherite_road_construction_tool", () -> new RoadConstructionTool(Tiers.NETHERITE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> HAMMER = ITEMS.register("hammer", () -> new HammerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> STREET_LAMP_CONFIG_CARD = ITEMS.register("street_lamp_config_card", () -> new StreetLampConfigCardItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> COLOR_PALETTE = ITEMS.register("color_palette", () -> new ColorPaletteItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> PATTERN_CATALOGUE = ITEMS.register("pattern_catalogue", () -> new PatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistrySupplier<Item> CREATIVE_PATTERN_CATALOGUE = ITEMS.register("creative_pattern_catalogue", () -> new CreativePatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));


    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(TrafficCraft.MOD_ID, Registry.RECIPE_SERIALIZER_REGISTRY);
    public static final RegistrySupplier<RecipeSerializer<?>> DAMAGEABLE_ITEM_RECIPE = RECIPES.register("damageable_item_recipe", DamageableItemRecipeSerializer::new);



    public static void register() {
        ITEMS.register();
        RECIPES.register();
    }

}
