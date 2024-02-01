package de.mrjulsen.trafficcraft.config;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_DISTANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ROAD_BUILDER_MAX_ROAD_WIDTH;
    public static final ForgeConfigSpec.ConfigValue<Double> ROAD_BUILDER_MAX_SLOPE;

    static {
        BUILDER.push(ModMain.MOD_ID + "_common_config");
            

        ROAD_BUILDER_MAX_DISTANCE = BUILDER.comment("The max distance in blocks the road construction tool can be used for. Default: 32 blocks")
            .defineInRange("road_construction_tool.max_distance", 32, 0, 64);

        ROAD_BUILDER_MAX_ROAD_WIDTH = BUILDER.comment("Max width of roads built by the road construction tool. Default: 9 blocks.")
            .defineInRange("road_construction_tool.max_width", 9, 1, 15);
        
        ROAD_BUILDER_MAX_SLOPE = BUILDER.comment("Max slope of roads built by the road construction tool. Default: 4.0")
            .define("road_construction_tool.max_slope", 4.0D);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
