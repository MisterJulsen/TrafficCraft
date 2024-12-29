package de.mrjulsen.trafficcraft.components;

import java.util.List;
import java.util.ArrayList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PatternCatalogueComponent(List<NamedTrafficSignTextureReference> textures, int selectedIndex) {

    public static final Codec<PatternCatalogueComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            Codec.list(NamedTrafficSignTextureReference.CODEC.codec()).optionalFieldOf("textures", new ArrayList<>()).forGetter(PatternCatalogueComponent::textures),
            Codec.INT.optionalFieldOf("selected_texture", 0).forGetter(PatternCatalogueComponent::selectedIndex)
        ).apply(builder, PatternCatalogueComponent::new);
    });
    
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternCatalogueComponent> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(NamedTrafficSignTextureReference::toNetwork, NamedTrafficSignTextureReference::fromNetwork).apply(ByteBufCodecs.list()), PatternCatalogueComponent::textures,
        ByteBufCodecs.INT, PatternCatalogueComponent::selectedIndex,
        (a, b) -> {
            return new PatternCatalogueComponent(new ArrayList<>(a), b);
        }
    );

    public static PatternCatalogueComponent empty() {
        return new PatternCatalogueComponent(
            new ArrayList<>(),
            0
        );
    }
}
