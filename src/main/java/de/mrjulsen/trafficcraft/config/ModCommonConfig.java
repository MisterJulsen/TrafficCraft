package de.mrjulsen.trafficcraft.config;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_MIN_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_MAX_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_RARITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_VEIN_SIZE;

    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_ROAD_WIDTH;
    public static final ForgeConfigSpec.ConfigValue<Double> ROAD_BUILDER_MAX_SLOPE;

    static {
        BUILDER.push(ModMain.MOD_ID + "_common_config");
        
        WORLD_BITUMEN_MIN_HEIGHT = BUILDER.comment("Min generation height for bitumen ore. Default: 55 blocks")
            .define("world_generation.bitumen_ore_min_height", 55);

        WORLD_BITUMEN_MAX_HEIGHT = BUILDER.comment("Max generation height for bitumen ore. Default: 75 blocks")
            .define("world_generation.bitumen_ore_max_height", 75);

        WORLD_BITUMEN_RARITY = BUILDER.comment("How common bitumen ore will be generated in each chunk. Default: 2")
            .define("world_generation.bitumen_ore_rarity", 2);

        WORLD_BITUMEN_VEIN_SIZE = BUILDER.comment("Max size of bitumen ore veins. Default: 25")
            .define("world_generation.bitumen_ore_vein_size", 25);
            

        ROAD_BUILDER_MAX_DISTANCE = BUILDER.comment("The max distance in blocks the road building gadget can be used for. Default: 32 blocks")
            .defineInRange("road_builder_gadget.max_distance", 32, 0, 64);

        ROAD_BUILDER_MAX_ROAD_WIDTH = BUILDER.comment("Max width of roads built by the road builder gadget. Default: 9 blocks.")
            .defineInRange("road_builder_gadget.max_width", 9, 1, 15);
        
        ROAD_BUILDER_MAX_SLOPE = BUILDER.comment("Max slope of roads built by the road builder gadget. Default: 4.0")
            .define("road_builder_gadget.max_slope", 4.0D);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
