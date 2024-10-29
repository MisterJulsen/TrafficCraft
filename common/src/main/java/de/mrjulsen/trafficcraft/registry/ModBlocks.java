package de.mrjulsen.trafficcraft.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.AsphaltBlock;
import de.mrjulsen.trafficcraft.block.AsphaltCurb;
import de.mrjulsen.trafficcraft.block.AsphaltCurbSlope;
import de.mrjulsen.trafficcraft.block.AsphaltSlope;
import de.mrjulsen.trafficcraft.block.ConcreteBarrierBlock;
import de.mrjulsen.trafficcraft.block.DelineatorBlock;
import de.mrjulsen.trafficcraft.block.FluorescentTubeLampBlock;
import de.mrjulsen.trafficcraft.block.GuardrailBlock;
import de.mrjulsen.trafficcraft.block.HouseNumberSignBlock;
import de.mrjulsen.trafficcraft.block.ManholeBlock;
import de.mrjulsen.trafficcraft.block.ManholeCoverBlock;
import de.mrjulsen.trafficcraft.block.PaintBucketBlock;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltBlock;
import de.mrjulsen.trafficcraft.block.PaintedAsphaltSlope;
import de.mrjulsen.trafficcraft.block.ReflectorBlock;
import de.mrjulsen.trafficcraft.block.RoadBarrierFenceBlock;
import de.mrjulsen.trafficcraft.block.RoadSaltBlock;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.block.StreetLightBlock;
import de.mrjulsen.trafficcraft.block.StreetSignBlock;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficBarrelBlock;
import de.mrjulsen.trafficcraft.block.TrafficBollardBlock;
import de.mrjulsen.trafficcraft.block.TrafficConeBlock;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.TrafficLightControllerBlock;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignPostBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignWorkbenchBlock;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock.LampType;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.WearableBlockItem;
import dev.architectury.extensions.injected.InjectedItemPropertiesExtension;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.BLOCK);
    public static List<RegistrySupplier<Block>> COLORED_BLOCKS = new ArrayList<>();
    public static HashMap<String, RegistrySupplier<Block>> ROAD_BLOCKS = new HashMap<>();    
    
    public static final RegistrySupplier<Block> BITUMEN_ORE = registerBlock("bitumen_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
        .strength(3f)
        .requiresCorrectToolForDrops()
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> DEEPSLATE_BITUMEN_ORE = registerBlock("deepslate_bitumen_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
        .strength(4.5f)
        .requiresCorrectToolForDrops()
        .sound(SoundType.DEEPSLATE)
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> BITUMEN_BLOCK = registerBlock("bitumen_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
        .strength(1.5f)
        .requiresCorrectToolForDrops()
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> SALT = registerBlock("salt", () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
        .strength(3f)
        .sound(SoundType.BASALT)
        .requiresCorrectToolForDrops()
    ), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> TRAFFIC_SIGN_WORKBENCH = registerBlock("traffic_sign_workbench", () -> new TrafficSignWorkbenchBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ASPHALT = registerBlock("asphalt", () -> new AsphaltBlock(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> CONCRETE = registerBlock("concrete", () -> new AsphaltBlock(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ASPHALT_SLOPE = registerBlock("asphalt_slope", () -> new AsphaltSlope(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> CONCRETE_SLOPE = registerBlock("concrete_slope", () -> new AsphaltSlope(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ASPHALT_CURB = registerBlock("asphalt_curb", () -> new AsphaltCurb(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> CONCRETE_CURB = registerBlock("concrete_curb", () -> new AsphaltCurb(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ASPHALT_CURB_SLOPE = registerBlock("asphalt_curb_slope", () -> new AsphaltCurbSlope(RoadType.ASPHALT), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> CONCRETE_CURB_SLOPE = registerBlock("concrete_curb_slope", () -> new AsphaltCurbSlope(RoadType.CONCRETE), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ROAD_SALT = registerBlock("road_salt", () -> new RoadSaltBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> CONCRETE_BARRIER = registerColoredBlock("concrete_barrier", () -> new ConcreteBarrierBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);

    static
    {
        for (RoadType s : RoadType.values()) {
            if (s == RoadType.NONE)
                continue;

            for (int i = 0; i < Constants.MAX_ASPHALT_PATTERNS; i++) {
                String id = s.getRoadType() + "_pattern_" + i;
                RegistrySupplier<Block> block = registerColoredBlockWithoutItem(id, () -> new PaintedAsphaltBlock(BlockBehaviour.Properties.of(), s));
                ROAD_BLOCKS.put(id, block);

                String id2 = s.getRoadType() + "_slope_pattern_" + i;
                RegistrySupplier<Block> block2 = registerColoredBlockWithoutItem(id2, () -> new PaintedAsphaltSlope(BlockBehaviour.Properties.of(), s));
                ROAD_BLOCKS.put(id2, block2);
            }
        }
    }

    public static final RegistrySupplier<Block> MANHOLE = registerBlock("manhole", () -> new ManholeBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> MANHOLE_COVER = registerBlock("manhole_cover", () -> new ManholeCoverBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> ROAD_GULLY = registerBlock("road_gully", () -> new ManholeCoverBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TRAFFIC_SIGN_POST = registerBlock("traffic_sign_post", () -> new TrafficSignPostBlock(), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TRAFFIC_SIGN = registerBlock("traffic_sign", () -> new TrafficSignBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TOWN_SIGN = registerBlock("town_sign", () -> new TownSignBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> STREET_SIGN = registerColoredBlock("street_sign", () -> new StreetSignBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> HOUSE_NUMBER_SIGN = registerColoredBlock("house_number_sign", () -> new HouseNumberSignBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TRAFFIC_LIGHT = registerColoredBlock("traffic_light", () -> new TrafficLightBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TRAFFIC_LIGHT_CONTROLLER = registerBlock("traffic_light_controller", () -> new TrafficLightControllerBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> TRAFFIC_LIGHT_REQUEST_BUTTON = registerBlock("traffic_light_request_button", () -> new TrafficLightRequestButtonBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> STREET_LAMP = registerBlock("street_lamp", () -> new StreetLampBaseBlock(BlockBehaviour.Properties.of(), LampType.NORMAL), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> DOUBLE_STREET_LAMP = registerBlock("double_street_lamp", () -> new StreetLampBaseBlock(BlockBehaviour.Properties.of(), LampType.DOUBLE), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> SMALL_STREET_LAMP = registerBlock("small_street_lamp", () -> new StreetLampBaseBlock(BlockBehaviour.Properties.of(), LampType.SMALL), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> SMALL_DOUBLE_STREET_LAMP = registerBlock("small_double_street_lamp", () -> new StreetLampBaseBlock(BlockBehaviour.Properties.of(), LampType.SMALL_DOUBLE), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> STREET_LIGHT = registerBlock("street_light", () -> new StreetLightBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> FLUORESCENT_TUBE_LAMP = registerBlock("fluorescent_tube_lamp", () -> new FluorescentTubeLampBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    
    public static final RegistrySupplier<Block> WHITE_DELINEATOR = registerBlock("white_delineator", () -> new DelineatorBlock(false), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> YELLOW_DELINEATOR = registerBlock("yellow_delineator", () -> new DelineatorBlock(false), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> RED_DELINEATOR = registerBlock("red_delineator", () -> new DelineatorBlock(false), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> SMALL_WHITE_DELINEATOR = registerBlock("small_white_delineator", () -> new DelineatorBlock(true), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> SMALL_YELLOW_DELINEATOR = registerBlock("small_yellow_delineator", () -> new DelineatorBlock(true), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> GUARDRAIL = registerColoredBlock("guardrail", () -> new GuardrailBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);

    public static final RegistrySupplier<Block> PAINT_BUCKET = registerColoredBlock("paint_bucket", () -> new PaintBucketBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, true);
    
    public static final RegistrySupplier<Block> TRAFFIC_CONE = registerColoredBlock("traffic_cone", () -> new TrafficConeBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> TRAFFIC_BOLLARD = registerColoredBlock("traffic_bollard", () -> new TrafficBollardBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> TRAFFIC_BARREL = registerColoredBlock("traffic_barrel", () -> new TrafficBarrelBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, true);
    public static final RegistrySupplier<Block> ROAD_BARRIER_FENCE = registerColoredBlock("road_barrier_fence", () -> new RoadBarrierFenceBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);
    public static final RegistrySupplier<Block> REFLECTOR = registerColoredBlock("reflector", () -> new ReflectorBlock(BlockBehaviour.Properties.of()), ModCreativeModeTab.MOD_TAB, false);

    


    private static <T extends Block>RegistrySupplier<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }

    private static <T extends Block>RegistrySupplier<Block> registerColoredBlockWithoutItem(String name, Supplier<Block> block) {
        RegistrySupplier<Block> toReturn = registerBlockWithoutItem(name, block);
        COLORED_BLOCKS.add(toReturn);
        return toReturn;
    }
    
    private static <T extends Block>RegistrySupplier<Block> registerColoredBlock(String name, Supplier<Block> block, RegistrySupplier<CreativeModeTab> tab, boolean wearable) {
        RegistrySupplier<Block> toReturn = registerBlock(name, block, tab, wearable);
        COLORED_BLOCKS.add(toReturn);
        return toReturn;
    }

    private static <T extends Block>RegistrySupplier<T> registerBlock(String name, Supplier<T> block, RegistrySupplier<CreativeModeTab> tab, boolean wearable) {
        RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab, wearable);
        return toReturn;
    }

    @SuppressWarnings("unused")
    private static <T extends Block, I extends BlockItem>RegistrySupplier<T> registerBlock(String name, Supplier<T> block, RegistrySupplier<CreativeModeTab> tab, Class<I> blockItemClass) {
        RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab, blockItemClass);
        return toReturn;
    }

    @SuppressWarnings("unused")
    private static <T extends Block>RegistrySupplier<T> registerBlockWithCustomItemId(String name, String itemId, Supplier<T> block, RegistrySupplier<CreativeModeTab> tab, boolean wearable) {
        RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(itemId, toReturn, tab, wearable);
        return toReturn;
    }

    private static <T extends Block>RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block, RegistrySupplier<CreativeModeTab> tab, boolean wearable) {
        if (wearable) {
            return ModItems.ITEMS.register(name, () -> new WearableBlockItem(block.get(), ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(tab)));
        }

        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(tab)));
    }

    private static <T extends Block, I extends BlockItem>RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block, RegistrySupplier<CreativeModeTab> tab, Class<I> blockItemClass) {
        return ModItems.ITEMS.register(name, () -> {
            try {
                return blockItemClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block.get(), ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(tab));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                return new BlockItem(block.get(), ((InjectedItemPropertiesExtension)new Item.Properties()).arch$tab(tab));
            }
        });
    }

    public static void register() {
        BLOCKS.register();
    }

}
