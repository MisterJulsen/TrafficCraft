package de.mrjulsen.trafficcraft.components;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.mcdragonlib.data.WorldLocation;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem.LinkerMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TrafficLightLinkerComponent(Optional<WorldLocation> location, LinkerMode mode, Optional<String> targetBlockName) {

    public static final Codec<TrafficLightLinkerComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            CompoundTag.CODEC.optionalFieldOf("location").xmap((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)).forGetter(TrafficLightLinkerComponent::location),
            Codec.INT.fieldOf("mode").xmap(LinkerMode::getByIndex, LinkerMode::getIndex).forGetter(TrafficLightLinkerComponent::mode),
            Codec.STRING.optionalFieldOf("target_block_name").forGetter(TrafficLightLinkerComponent::targetBlockName)
        ).apply(builder, TrafficLightLinkerComponent::new);
    });
    
    public static final StreamCodec<ByteBuf, TrafficLightLinkerComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs::optional).map((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)), TrafficLightLinkerComponent::location,
        ByteBufCodecs.INT.map(LinkerMode::getByIndex, LinkerMode::getIndex), TrafficLightLinkerComponent::mode,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), TrafficLightLinkerComponent::targetBlockName,
        TrafficLightLinkerComponent::new
    );

    public static TrafficLightLinkerComponent empty() {
        return new TrafficLightLinkerComponent(Optional.empty(), LinkerMode.LINK, Optional.empty());
    }
}
