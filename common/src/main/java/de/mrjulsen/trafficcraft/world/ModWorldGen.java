package de.mrjulsen.trafficcraft.world;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import dev.architectury.platform.Platform;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

public class ModWorldGen {
    
    public static void init() {
        if (Platform.isFabric()) {
            BiomeModifications.addProperties((ctx, mutable) -> {
                if (ModCommonConfig.BITUMEN_GENERATION.get()) { 
                    mutable.getGenerationProperties().addFeature(Decoration.UNDERGROUND_ORES, PlacementUtils.createKey(TrafficCraft.MOD_ID + ":bitumen_ore_placed"));
                }            
                if (ModCommonConfig.SALT_GENERATION.get() && ctx.hasTag(BiomeTags.IS_OCEAN)) {                
                    mutable.getGenerationProperties().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, PlacementUtils.createKey(TrafficCraft.MOD_ID + ":disk_salt_placed"));
                }
            });
        }
    }
}
