package de.mrjulsen.trafficcraft.world.feature;

import java.util.List;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPlacedFeatures {
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, ModMain.MOD_ID);

    public static final RegistryObject<PlacedFeature> BITUMEN_ORE_PLACED = PLACED_FEATURES.register("bitumen_ore_placed",
        () -> new PlacedFeature(ModConfiguredFeatures.BITUMEN_ORE.getHolder().get(),
            commonOrePlacement(ModCommonConfig.WORLD_BITUMEN_RARITY.get(), // VeinsPerChunk
                HeightRangePlacement.triangle(VerticalAnchor.absolute(ModCommonConfig.WORLD_BITUMEN_MIN_HEIGHT.get()), VerticalAnchor.absolute(ModCommonConfig.WORLD_BITUMEN_MAX_HEIGHT.get())))));

    public static List<PlacementModifier> orePlacement(PlacementModifier p_195347_, PlacementModifier p_195348_) {
        return List.of(p_195347_, InSquarePlacement.spread(), p_195348_, BiomeFilter.biome());
    }

    public static List<PlacementModifier> commonOrePlacement(int p_195344_, PlacementModifier p_195345_) {
        return orePlacement(CountPlacement.of(p_195344_), p_195345_);
    }

    public static List<PlacementModifier> rareOrePlacement(int p_195350_, PlacementModifier p_195351_) {
        return orePlacement(RarityFilter.onAverageOnceEvery(p_195350_), p_195351_);
    }

    public static void register(IEventBus eventBus) {
        PLACED_FEATURES.register(eventBus);
    }
}
