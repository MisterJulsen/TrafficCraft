package de.mrjulsen.trafficcraft.block;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Supplier;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock.LampType;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import de.mrjulsen.trafficcraft.item.ModCreativeModeTab;
import de.mrjulsen.trafficcraft.item.ModItems;
import de.mrjulsen.trafficcraft.item.WearableBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModMain.MOD_ID);
    public static List<RegistryObject<Block>> COLORED_BLOCKS = new ArrayList<>();
    public static HashMap<String, RegistryObject<Block>> ROAD_BLOCKS = new HashMap<>();    
    
    public static final RegistryObject<Block> DRAGON = registerBlock("dragon", () -> new DragonBlock(BlockBehaviour.Properties.of(Material.STONE)
        .strength(1.5f)
    ), null, DragonBlock.DragonItem.class);
    
    public static final RegistryObject<Block> BITUMEN_ORE = registerBlock("bitumen_ore", () -> new OreBlock(BlockBehaviour.Properties.of(Material.STONE)
        .strength(3f)
        .requiresCorrectToolForDrops()
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistryObject<Block> DEEPSLATE_BITUMEN_ORE = registerBlock("deepslate_bitumen_ore", () -> new OreBlock(BlockBehaviour.Properties.of(Material.STONE)
        .strength(4.5f)
        .requiresCorrectToolForDrops()
        .sound(SoundType.DEEPSLATE)
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistryObject<Block> BITUMEN_BLOCK = registerBlock("bitumen_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)
        .strength(1.5f)
        .requiresCorrectToolForDrops()
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistryObject<Block> ASPHALT = registerBlock("asphalt", () -> new AsphaltBlock(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CONCRETE = registerBlock("concrete", () -> new AsphaltBlock(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> ASPHALT_SLOPE = registerBlock("asphalt_slope", () -> new AsphaltSlope(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CONCRETE_SLOPE = registerBlock("concrete_slope", () -> new AsphaltSlope(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> ASPHALT_CURB = registerBlock("asphalt_curb", () -> new AsphaltCurb(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CONCRETE_CURB = registerBlock("concrete_curb", () -> new AsphaltCurb(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> ASPHALT_CURB_SLOPE = registerBlock("asphalt_curb_slope", () -> new AsphaltCurbSlope(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CONCRETE_CURB_SLOPE = registerBlock("concrete_curb_slope", () -> new AsphaltCurbSlope(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CONCRETE_BARRIER = registerColoredBlock("concrete_barrier", () -> new ConcreteBarrierBlock(), ModCreativeModeTab.MOD_TAB, false);

    static
    {
        for (RoadType s : RoadType.values()) {
            if (s == RoadType.NONE)
                continue;

            for (int i = 0; i < Constants.MAX_ASPHALT_PATTERNS; i++) {
                String id = s.getRoadType() + "_pattern_" + i;
                RegistryObject<Block> block = registerColoredBlockWithoutItem(id, () -> new PaintedAsphaltBlock(s, s == RoadType.ASPHALT ? ModBlocks.ASPHALT.get() : ModBlocks.CONCRETE.get()));
                ROAD_BLOCKS.put(id, block);

                String id2 = s.getRoadType() + "_slope_pattern_" + i;
                RegistryObject<Block> block2 = registerColoredBlockWithoutItem(id2, () -> new PaintedAsphaltSlope(s, s == RoadType.ASPHALT ? ModBlocks.ASPHALT_SLOPE.get() : ModBlocks.CONCRETE_SLOPE.get()));
                ROAD_BLOCKS.put(id2, block2);
            }
        }
    }

    public static final RegistryObject<Block> MANHOLE = registerBlock("manhole", () -> new ManholeBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> MANHOLE_COVER = registerBlock("manhole_cover", () -> new ManholeCoverBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> ROAD_GULLY = registerBlock("road_gully", () -> new ManholeCoverBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> TRAFFIC_SIGN_POST = registerBlock("traffic_sign_post", () -> new TrafficSignPostBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> CIRCLE_TRAFFIC_SIGN = registerBlockWithCustomItemId("circle_traffic_sign", "traffic_sign", () -> new CircleTrafficSignBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> TRIANGLE_TRAFFIC_SIGN = registerBlockWithoutItem("triangle_traffic_sign", () -> new TriangleTrafficSignBlock());
    public static final RegistryObject<Block> SQUARE_TRAFFIC_SIGN = registerBlockWithoutItem("square_traffic_sign", () -> new SquareTrafficSignBlock());
    public static final RegistryObject<Block> DIAMOND_TRAFFIC_SIGN = registerBlockWithoutItem("diamond_traffic_sign", () -> new DiamondTrafficSignBlock());
    public static final RegistryObject<Block> RECTANGLE_TRAFFIC_SIGN = registerBlockWithoutItem("rectangle_traffic_sign", () -> new RectangleTrafficSignBlock());
    public static final RegistryObject<Block> MISC_TRAFFIC_SIGN = registerBlockWithoutItem("misc_traffic_sign", () -> new MiscTrafficSignBlock());
    public static final RegistryObject<Block> TOWN_SIGN = registerBlock("town_sign", () -> new TownSignBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> STREET_SIGN = registerColoredBlock("street_sign", () -> new StreetSignBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> TRAFFIC_LIGHT = registerBlock("traffic_light", () -> new TrafficLightBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> TRAFFIC_LIGHT_CONTROLLER = registerBlock("traffic_light_controller", () -> new TrafficLightControllerBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> TRAFFIC_LIGHT_REQUEST_BUTTON = registerBlock("traffic_light_request_button", () -> new TrafficLightRequestButtonBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> STREET_LAMP = registerBlock("street_lamp", () -> new StreetLampBaseBlock(LampType.NORMAL), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistryObject<Block> DOUBLE_STREET_LAMP = registerBlock("double_street_lamp", () -> new StreetLampBaseBlock(LampType.DOUBLE), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistryObject<Block> SMALL_STREET_LAMP = registerBlock("small_street_lamp", () -> new StreetLampBaseBlock(LampType.SMALL), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistryObject<Block> SMALL_DOUBLE_STREET_LAMP = registerBlock("small_double_street_lamp", () -> new StreetLampBaseBlock(LampType.SMALL_DOUBLE), ModCreativeModeTab.MOD_TAB, true);
    
    public static final RegistryObject<Block> WHITE_DELINEATOR = registerBlock("white_delineator", () -> new DelineatorBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> YELLOW_DELINEATOR = registerBlock("yellow_delineator", () -> new DelineatorBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> RED_DELINEATOR = registerBlock("red_delineator", () -> new DelineatorBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> SMALL_WHITE_DELINEATOR = registerBlock("small_white_delineator", () -> new DelineatorBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> SMALL_YELLOW_DELINEATOR = registerBlock("small_yellow_delineator", () -> new DelineatorBlock(), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistryObject<Block> GUARDRAIL = registerColoredBlock("guardrail", () -> new GuardrailBlock(), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistryObject<Block> PAINT_BUCKET = registerColoredBlock("paint_bucket", () -> new PaintBucketBlock(), ModCreativeModeTab.MOD_TAB, true);
    
    public static final RegistryObject<Block> TRAFFIC_CONE = registerColoredBlock("traffic_cone", () -> new TrafficConeBlock(), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistryObject<Block> TRAFFIC_BOLLARD = registerColoredBlock("traffic_bollard", () -> new TrafficBollardBlock(), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistryObject<Block> ROAD_BARRIER_FENCE = registerColoredBlock("road_barrier_fence", () -> new RoadBarrierFenceBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistryObject<Block> REFLECTOR = registerColoredBlock("reflector", () -> new ReflectorBlock(), ModCreativeModeTab.MOD_TAB, false);

    


    private static <T extends Block>RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Block> registerColoredBlockWithoutItem(String name, Supplier<Block> block) {
        RegistryObject<Block> toReturn = registerBlockWithoutItem(name, block);
        COLORED_BLOCKS.add(toReturn);
        return toReturn;
    }
    
    private static <T extends Block>RegistryObject<Block> registerColoredBlock(String name, Supplier<Block> block, CreativeModeTab tab, boolean wearable) {
        RegistryObject<Block> toReturn = registerBlock(name, block, tab, wearable);
        COLORED_BLOCKS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab, boolean wearable) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab, wearable);
        return toReturn;
    }

    private static <T extends Block, I extends BlockItem>RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab, Class<I> blockItemClass) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab, blockItemClass);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<T> registerBlockWithCustomItemId(String name, String itemId, Supplier<T> block, CreativeModeTab tab, boolean wearable) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(itemId, toReturn, tab, wearable);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab, boolean wearable) {
        if (wearable) {
            return ModItems.ITEMS.register(name, () -> new WearableBlockItem(block.get(), new Item.Properties().tab(tab)));
        }

        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    private static <T extends Block, I extends BlockItem>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab, Class<I> blockItemClass) {
        return ModItems.ITEMS.register(name, () -> {
            try {
                return blockItemClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block.get(), new Item.Properties().tab(tab));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                return new BlockItem(block.get(), new Item.Properties().tab(tab));
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
