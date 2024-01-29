package de.mrjulsen.trafficcraft.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
    public static final List<RegistryObject<Item>> COLORED_ITEMS = new ArrayList<>();    

    public static final RegistryObject<Item> WRENCH = registerItem("wrench", () -> new WrenchItem());
    public static final RegistryObject<Item> TRAFFIC_LIGHT_LINKER = registerItem("traffic_light_linker", () -> new TrafficLightLinkerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB).stacksTo(1)));
    public static final RegistryObject<Item> BITUMEN = registerItem("raw_bitumen", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_ROD = registerItem("iron_rod", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_PLATE = registerItem("iron_plate", () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> PAINT_BRUSH = registerColoredItem("paint_brush", () -> new BrushItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB),0));
    public static final RegistryObject<Item> WOOD_ROAD_CONSTRUCTION_TOOL = registerItem("wood_road_construction_tool", () -> new RoadConstructionTool(Tiers.WOOD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> STONE_ROAD_CONSTRUCTION_TOOL = registerItem("stone_road_construction_tool", () -> new RoadConstructionTool(Tiers.STONE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> IRON_ROAD_CONSTRUCTION_TOOL = registerItem("iron_road_construction_tool", () -> new RoadConstructionTool(Tiers.IRON, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> GOLD_ROAD_CONSTRUCTION_TOOL = registerItem("gold_road_construction_tool", () -> new RoadConstructionTool(Tiers.GOLD, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> DIAMOND_ROAD_CONSTRUCTION_TOOL = registerItem("diamond_road_construction_tool", () -> new RoadConstructionTool(Tiers.DIAMOND, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> NETHERITE_ROAD_CONSTRUCTION_TOOL = registerItem("netherite_road_construction_tool", () -> new RoadConstructionTool(Tiers.NETHERITE, new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> HAMMER = registerItem("hammer", () -> new HammerItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> STREET_LAMP_CONFIG_CARD = registerItem("street_lamp_config_card", () -> new StreetLampConfigCardItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> COLOR_PALETTE = registerColoredItem("color_palette", () -> new ColorPaletteItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> PATTERN_CATALOGUE = registerItem("pattern_catalogue", () -> new PatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));
    public static final RegistryObject<Item> CREATIVE_PATTERN_CATALOGUE = registerItem("creative_pattern_catalogue", () -> new CreativePatternCatalogueItem(new Item.Properties().tab(ModCreativeModeTab.MOD_TAB)));

    private static RegistryObject<Item> registerItem(String id, Supplier<? extends Item> sup) {
        return ITEMS.register(id, sup);
    }

    private static RegistryObject<Item> registerColoredItem(String id, Supplier<? extends Item> sup) {
        RegistryObject<Item> item = ITEMS.register(id, sup);
        COLORED_ITEMS.add(item);
        return item;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus); 
    }

}
