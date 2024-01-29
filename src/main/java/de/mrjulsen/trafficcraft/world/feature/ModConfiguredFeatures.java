package de.mrjulsen.trafficcraft.world.feature;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModConfiguredFeatures {

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, ModMain.MOD_ID);

    public static void register(IEventBus eventBus) {
        CONFIGURED_FEATURES.register(eventBus);
    }
    
    public static final Supplier<List<OreConfiguration.TargetBlockState>> OVERWORLD_BITUMEN_ORES = Suppliers.memoize(() -> List.of(
        OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, ModBlocks.BITUMEN_ORE.get().defaultBlockState()),
        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, ModBlocks.DEEPSLATE_BITUMEN_ORE.get().defaultBlockState())));

    public static final RegistryObject<ConfiguredFeature<?, ?>> BITUMEN_ORE = CONFIGURED_FEATURES.register("bitumen_ore",
        () -> new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(OVERWORLD_BITUMEN_ORES.get(), ModCommonConfig.WORLD_BITUMEN_VEIN_SIZE.get())));
}
