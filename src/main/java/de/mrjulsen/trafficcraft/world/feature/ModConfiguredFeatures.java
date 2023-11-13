package de.mrjulsen.trafficcraft.world.feature;

import java.util.List;

import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ModConfiguredFeatures {
    
    public static final List<OreConfiguration.TargetBlockState> OVERWORLD_BITUMEN_ORES = List.of(
        OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, ModBlocks.BITUMEN_ORE.get().defaultBlockState()),
        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, ModBlocks.DEEPSLATE_BITUMEN_ORE.get().defaultBlockState()));

    public static final Holder<ConfiguredFeature<OreConfiguration, ?>> BITUMEN_ORE = FeatureUtils.register("bitumen_ore",
        Feature.ORE, new OreConfiguration(OVERWORLD_BITUMEN_ORES, 25));
}
