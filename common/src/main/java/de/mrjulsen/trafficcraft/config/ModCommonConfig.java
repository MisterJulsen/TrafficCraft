package de.mrjulsen.trafficcraft.config;

import de.mrjulsen.trafficcraft.TrafficCraft;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> BITUMEN_GENERATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_MIN_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_MAX_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_RARITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_BITUMEN_VEIN_SIZE;
    
    public static final ForgeConfigSpec.ConfigValue<Boolean> SALT_GENERATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_SALT_DISK_MIN_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_SALT_DISK_MAX_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_SALT_DISK_HALF_HEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WORLD_SALT_RARITY;

    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_ROAD_WIDTH;
    public static final ForgeConfigSpec.ConfigValue<Double> ROAD_BUILDER_MAX_SLOPE;

    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_SALT_PRESERVATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_SALT_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_SALT_SPEED;

    static {
        BUILDER.push(TrafficCraft.MOD_ID + "_common_config");
        
        BITUMEN_GENERATION = BUILDER.comment("Whether bitumen ore should generate in the world or not. (Default: ON)")
            .define("world_generation.bitumen.enabled", true);
        WORLD_BITUMEN_MIN_HEIGHT = BUILDER.comment("Min generation height for bitumen ore. (Default: 55)")
            .define("world_generation.bitumen.min_height", 55);
        WORLD_BITUMEN_MAX_HEIGHT = BUILDER.comment("Max generation height for bitumen ore. (Default: 75)")
            .define("world_generation.bitumen.max_height", 75);
        WORLD_BITUMEN_RARITY = BUILDER.comment("How common bitumen ore will be generated in each chunk. (Default: 2)")
            .define("world_generation.bitumen.rarity", 2);
        WORLD_BITUMEN_VEIN_SIZE = BUILDER.comment("Max size of bitumen ore veins. (Default: 25)")
            .define("world_generation.bitumen.vein_size", 25);
            
        
        SALT_GENERATION = BUILDER.comment("Whether salt should generate in the world or not. (Default: ON)")
            .define("world_generation.salt.enabled", true);
        WORLD_SALT_DISK_MIN_RADIUS = BUILDER.comment("Min generation height for bitumen ore. (Default: 5)")
            .define("world_generation.salt.disk_min_radius", 5);
        WORLD_SALT_DISK_MAX_RADIUS = BUILDER.comment("Max generation height for bitumen ore. (Default: 8)")
            .define("world_generation.salt.disk_max_radius", 8);
        WORLD_SALT_DISK_HALF_HEIGHT = BUILDER.comment("How common bitumen ore will be generated in each chunk. (Default: 1)")
            .define("world_generation.salt.disk_half_height", 1);
        WORLD_SALT_RARITY = BUILDER.comment("Max size of bitumen ore veins. (Default: 25)")
            .define("world_generation.salt.rarity", 15);
            

        ROAD_BUILDER_MAX_DISTANCE = BUILDER.comment("The max distance in blocks the road construction tool can be used for. (Default: 32)")
            .defineInRange("road_construction_tool.max_distance", 32, 0, 64);

        ROAD_BUILDER_MAX_ROAD_WIDTH = BUILDER.comment("Max width of roads built by the road construction tool. (Default: 9)")
            .defineInRange("road_construction_tool.max_width", 9, 1, 15);
        
        ROAD_BUILDER_MAX_SLOPE = BUILDER.comment("Max slope of roads built by the road construction tool. (Default: 4.0)")
            .define("road_construction_tool.max_slope", 4.0D);
        

        ROAD_SALT_PRESERVATION = BUILDER.comment("How quickly road salt should be used up. Set to -1 to disable (road salt always persists). Higher values increase durability. (Default: 64)")
            .defineInRange("gameplay.road_salt.preservation", 100, -1, Short.MAX_VALUE);
        ROAD_SALT_RANGE = BUILDER.comment("The range of road salt in which snow melts. (Default: 3)")
            .defineInRange("gameplay.road_salt.range", 3, 1, 8);
        ROAD_SALT_SPEED = BUILDER.comment("The base speed of the road salt. (Default: 100)")
            .defineInRange("gameplay.road_salt.speed", 100, 1, Short.MAX_VALUE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
