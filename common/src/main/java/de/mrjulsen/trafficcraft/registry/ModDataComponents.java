package de.mrjulsen.trafficcraft.registry;

import java.util.List;

import com.mojang.serialization.Codec;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.components.BrushComponent;
import de.mrjulsen.trafficcraft.components.CreativePatternCatalogueComponent;
import de.mrjulsen.trafficcraft.components.PatternCatalogueComponent;
import de.mrjulsen.trafficcraft.components.RoadConstructionToolComponent;
import de.mrjulsen.trafficcraft.components.StreetLampComponent;
import de.mrjulsen.trafficcraft.components.TrafficLightLinkerComponent;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<BrushComponent>> BRUSH_COMPONENT = COMPONENTS.register("paint_brush", () -> 
        new DataComponentType.Builder<BrushComponent>()
            .persistent(BrushComponent.CODEC)
            .networkSynchronized(BrushComponent.STREAM_CODEC)
            .build()
        );
        
    public static final RegistrySupplier<DataComponentType<List<Integer>>> COLOR_PALETTE_COMPONENT = COMPONENTS.register("color_palette_colors", () -> 
        new DataComponentType.Builder<List<Integer>>()
            .persistent(Codec.INT.listOf(ColorPaletteItem.MAX_COLORS, ColorPaletteItem.MAX_COLORS))
            .networkSynchronized(ByteBufCodecs.INT.apply(ByteBufCodecs.list(ColorPaletteItem.MAX_COLORS)))
            .build()
        );
        
    public static final RegistrySupplier<DataComponentType<PatternCatalogueComponent>> PATTERN_CATALOGUE_COMPONENT = COMPONENTS.register("pattern_catalogue", () -> 
        new DataComponentType.Builder<PatternCatalogueComponent>()
            .persistent(PatternCatalogueComponent.CODEC)
            .networkSynchronized(PatternCatalogueComponent.STREAM_CODEC)
            .build()
        );

    public static final RegistrySupplier<DataComponentType<CreativePatternCatalogueComponent>> CREATIVE_PATTERN_CATALOGUE_COMPONENT = COMPONENTS.register("creative_pattern_catalogue", () -> 
        new DataComponentType.Builder<CreativePatternCatalogueComponent>()
            .persistent(CreativePatternCatalogueComponent.CODEC)
            .networkSynchronized(CreativePatternCatalogueComponent.STREAM_CODEC)
            .build()
        );
         
    public static final RegistrySupplier<DataComponentType<RoadConstructionToolComponent>> ROAD_CONSTRUCTION_TOOL_COMPONENT = COMPONENTS.register("road_construction_tool", () -> 
        new DataComponentType.Builder<RoadConstructionToolComponent>()
            .persistent(RoadConstructionToolComponent.CODEC)
            .networkSynchronized(RoadConstructionToolComponent.STREAM_CODEC)
            .build()
        );
         
    public static final RegistrySupplier<DataComponentType<StreetLampComponent>> STREET_LAMP_COMPONENT = COMPONENTS.register("street_lamp_configuration", () -> 
        new DataComponentType.Builder<StreetLampComponent>()
            .persistent(StreetLampComponent.CODEC)
            .networkSynchronized(StreetLampComponent.STREAM_CODEC)
            .build()
        );
         
    public static final RegistrySupplier<DataComponentType<TrafficLightLinkerComponent>> TRAFFIC_LIGHT_LINKER_COMPONENT = COMPONENTS.register("traffic_light_linker", () -> 
        new DataComponentType.Builder<TrafficLightLinkerComponent>()
            .persistent(TrafficLightLinkerComponent.CODEC)
            .networkSynchronized(TrafficLightLinkerComponent.STREAM_CODEC)
            .build()
        );

    public static void init() {
        COMPONENTS.register();
    }
}
