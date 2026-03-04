package de.mrjulsen.trafficcraft.components;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.mcdragonlib.data.WorldLocation;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RoadConstructionToolComponent(Optional<WorldLocation> start, Optional<WorldLocation> end, RoadType type, byte roadWidth, boolean replaceBlocks) {

    public static final Codec<RoadConstructionToolComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            CompoundTag.CODEC.optionalFieldOf("start").xmap((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)).forGetter(RoadConstructionToolComponent::start),
            CompoundTag.CODEC.optionalFieldOf("end").xmap((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)).forGetter(RoadConstructionToolComponent::end),
            RoadType.CODEC.fieldOf("road_type").forGetter(RoadConstructionToolComponent::type),
            Codec.BYTE.fieldOf("road_width").forGetter(RoadConstructionToolComponent::roadWidth),
            Codec.BOOL.optionalFieldOf("replace_blocks", true).forGetter(RoadConstructionToolComponent::replaceBlocks)
        ).apply(builder, RoadConstructionToolComponent::new);
    });
    
    public static final StreamCodec<ByteBuf, RoadConstructionToolComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs::optional).map((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)), RoadConstructionToolComponent::start,
        ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs::optional).map((b) -> b.map(WorldLocation::loadFromNbt), (b) -> b.map(WorldLocation::toNbt)), RoadConstructionToolComponent::end,
        ByteBufCodecs.INT.map(RoadType::getRoadTypeByIndex, RoadType::getIndex), RoadConstructionToolComponent::type,
        ByteBufCodecs.BYTE, RoadConstructionToolComponent::roadWidth,
        ByteBufCodecs.BOOL, RoadConstructionToolComponent::replaceBlocks,
        RoadConstructionToolComponent::new
    );

    public static RoadConstructionToolComponent empty() {
        return new RoadConstructionToolComponent(
            Optional.empty(),
            Optional.empty(),
            RoadType.ASPHALT,
            (byte)RoadConstructionTool.getDefaultRoadWidth(),
            true
        );
    }
}
