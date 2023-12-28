package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.HammerItem;
import de.mrjulsen.trafficcraft.item.ModCreativeModeTab;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);


    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench", () -> new WrenchItem());
    public static final RegistryObject<Item> TRAFFIC_LIGHT_LINKER = ITEMS.register("traffic_light_linker", () -> new TrafficLightLinkerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> BITUMEN = ITEMS.register("raw_bitumen", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_ROD = ITEMS.register("iron_rod", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_PLATE = ITEMS.register("iron_plate", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new BrushItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB),0));
    public static final RegistryObject<Item> WOOD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("wood_road_construction_tool", () -> new RoadConstructionTool(Tiers.WOOD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> STONE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("stone_road_construction_tool", () -> new RoadConstructionTool(Tiers.STONE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_ROAD_CONSTRUCTION_TOOL = ITEMS.register("iron_road_construction_tool", () -> new RoadConstructionTool(Tiers.IRON, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> GOLD_ROAD_CONSTRUCTION_TOOL = ITEMS.register("gold_road_construction_tool", () -> new RoadConstructionTool(Tiers.GOLD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> DIAMOND_ROAD_CONSTRUCTION_TOOL = ITEMS.register("diamond_road_construction_tool", () -> new RoadConstructionTool(Tiers.DIAMOND, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> NETHERITE_ROAD_CONSTRUCTION_TOOL = ITEMS.register("netherite_road_construction_tool", () -> new RoadConstructionTool(Tiers.NETHERITE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer", () -> new HammerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> STREET_LAMP_CONFIG_CARD = ITEMS.register("street_lamp_config_card", () -> new StreetLampConfigCardItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> COLOR_PALETTE = ITEMS.register("color_palette", () -> new ColorPaletteItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> PATTERN_CATALOGUE = ITEMS.register("pattern_catalogue", () -> new PatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> CREATIVE_PATTERN_CATALOGUE = ITEMS.register("creative_pattern_catalogue", () -> new CreativePatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus); 
    }

}
