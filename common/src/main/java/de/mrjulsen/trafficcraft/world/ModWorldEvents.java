package de.mrjulsen.trafficcraft.world;

import java.util.List;

import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class ModWorldEvents {    
    
    public static final List<OreConfiguration.TargetBlockState> OVERWORLD_BITUMEN_ORES = List.of(
        OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, ModBlocks.BITUMEN_ORE.get().defaultBlockState()),
        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, ModBlocks.DEEPSLATE_BITUMEN_ORE.get().defaultBlockState()));

    /*
    public static final List<OreConfiguration.TargetBlockState> OVERWORLD_ROCK_SALT = List.of(
        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, ModBlocks.ROCK_SALT.get().defaultBlockState())
    );
    */



    public static final Holder<ConfiguredFeature<OreConfiguration, ?>> BITUMEN_ORE = FeatureUtils.register(TrafficCraft.MOD_ID + ":bitumen_ore",
        Feature.ORE, new OreConfiguration(OVERWORLD_BITUMEN_ORES, ModCommonConfig.WORLD_BITUMEN_VEIN_SIZE.get()));
    
    /* 
    public static final Holder<ConfiguredFeature<OreConfiguration, ?>> ROCK_SALT = FeatureUtils.register(TrafficCraft.MOD_ID + ":rock_salt",
        Feature.ORE, new OreConfiguration(OVERWORLD_ROCK_SALT, 64));
    */

    public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> SALT = FeatureUtils.register(TrafficCraft.MOD_ID + ":salt",
        Feature.DISK, new DiskConfiguration(ModBlocks.SALT.get().defaultBlockState(), UniformInt.of(ModCommonConfig.WORLD_SALT_DISK_MIN_RADIUS.get(), ModCommonConfig.WORLD_SALT_DISK_MAX_RADIUS.get()), ModCommonConfig.WORLD_SALT_DISK_HALF_HEIGHT.get(), List.of(Blocks.GRAVEL.defaultBlockState(), Blocks.SAND.defaultBlockState())));

    public static void init() {
           
        MutableSingle<Holder<PlacedFeature>> bitumenOrePlacedFeature = new MutableSingle<Holder<PlacedFeature>>(null);
        if (ModCommonConfig.BITUMEN_GENERATION.get()) { 
            bitumenOrePlacedFeature.setFirst(PlacementUtils.register(
                TrafficCraft.MOD_ID + ":bitumen_ores",
                BITUMEN_ORE,                
                List.of(
                    CountPlacement.of(ModCommonConfig.WORLD_BITUMEN_RARITY.get()),
                    InSquarePlacement.spread(),
                    HeightRangePlacement.triangle(VerticalAnchor.absolute(ModCommonConfig.WORLD_BITUMEN_MIN_HEIGHT.get()), VerticalAnchor.absolute(ModCommonConfig.WORLD_BITUMEN_MAX_HEIGHT.get())),
                    BiomeFilter.biome()
                )
            ));
        }

        MutableSingle<Holder<PlacedFeature>> saltDiskPlacedFeature = new MutableSingle<Holder<PlacedFeature>>(null);
        if (ModCommonConfig.BITUMEN_GENERATION.get()) { 
            saltDiskPlacedFeature.setFirst(PlacementUtils.register(
                TrafficCraft.MOD_ID + ":disk_salt",
                SALT,
                List.of(
                    RarityFilter.onAverageOnceEvery(ModCommonConfig.WORLD_SALT_RARITY.get()),
                    InSquarePlacement.spread(),
                    HeightmapPlacement.onHeightmap(Types.OCEAN_FLOOR_WG),
                    BiomeFilter.biome()
                )
            ));
        }
        /*
        Holder<PlacedFeature> rockSaltOrePlacedFeature = PlacementUtils.register(
            TrafficCraft.MOD_ID + ":ore_rock_salt",
            ROCK_SALT,
            List.of(
                RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.absolute(0)),
                BiomeFilter.biome()
            )
        );
        */
                        
        BiomeModifications.addProperties((ctx, mutable) -> {
            if (ModCommonConfig.BITUMEN_GENERATION.get() && bitumenOrePlacedFeature.getFirst() != null) { 
                mutable.getGenerationProperties().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, bitumenOrePlacedFeature.getFirst());
            }
            if (ModCommonConfig.SALT_GENERATION.get() && ctx.getProperties().getCategory() == BiomeCategory.OCEAN && saltDiskPlacedFeature.getFirst() != null) {                
                mutable.getGenerationProperties().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, saltDiskPlacedFeature.getFirst());
            }
            //mutable.getGenerationProperties().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, rockSaltOrePlacedFeature);            
        });
    }
}
