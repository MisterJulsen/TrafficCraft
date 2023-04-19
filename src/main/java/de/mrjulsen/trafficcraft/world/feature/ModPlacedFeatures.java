package de.mrjulsen.trafficcraft.world.feature;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModPlacedFeatures {
    public static final Holder<PlacedFeature> BITUMEN_ORE_PLACED = PlacementUtils.register("bitumen_ore_placed",
    ModConfiguredFeatures.BITUMEN_ORE, ModOrePlacement.commonOrePlacement(2, // VeinsPerChunk
            HeightRangePlacement.triangle(VerticalAnchor.absolute(55), VerticalAnchor.absolute(75))));
}
