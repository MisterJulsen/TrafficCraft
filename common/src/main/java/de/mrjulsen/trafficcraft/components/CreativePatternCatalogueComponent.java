package de.mrjulsen.trafficcraft.components;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CreativePatternCatalogueComponent(Optional<NamedTrafficSignTextureReference> customTexture) {

    public static final Codec<CreativePatternCatalogueComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            NamedTrafficSignTextureReference.CODEC.codec().optionalFieldOf("custom_texture").forGetter(CreativePatternCatalogueComponent::customTexture)
        ).apply(builder, CreativePatternCatalogueComponent::new);
    });
    
    public static final StreamCodec<RegistryFriendlyByteBuf, CreativePatternCatalogueComponent> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(NamedTrafficSignTextureReference::toNetwork, NamedTrafficSignTextureReference::fromNetwork).apply(ByteBufCodecs::optional), CreativePatternCatalogueComponent::customTexture,
        CreativePatternCatalogueComponent::new
    );

    public static CreativePatternCatalogueComponent empty() {
        return new CreativePatternCatalogueComponent(
            Optional.empty()
        );
    }
}
