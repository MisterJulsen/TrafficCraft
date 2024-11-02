package de.mrjulsen.trafficcraft.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.mcdragonlib.util.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StreetLampComponent(int turnOnTime, int turnOffTime, TimeFormat timeFormat) {

    public static final Codec<StreetLampComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            Codec.INT.optionalFieldOf("turn_on_time", StreetLampConfigCardItem.DEFAULT_TURN_ON_TIME).forGetter(StreetLampComponent::turnOnTime),
            Codec.INT.optionalFieldOf("turn_off_time", StreetLampConfigCardItem.DEFAULT_TURN_OFF_TIME).forGetter(StreetLampComponent::turnOffTime),
            Codec.BYTE.optionalFieldOf("time_format", TimeFormat.TICKS.getIndex()).xmap(TimeFormat::getFormatByIndex, TimeFormat::getIndex).forGetter(StreetLampComponent::timeFormat)
        ).apply(builder, StreetLampComponent::new);
    });
    
    public static final StreamCodec<RegistryFriendlyByteBuf, StreetLampComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, StreetLampComponent::turnOnTime,
        ByteBufCodecs.INT, StreetLampComponent::turnOffTime,
        ByteBufCodecs.BYTE.map(TimeFormat::getFormatByIndex, TimeFormat::getIndex), StreetLampComponent::timeFormat,
        StreetLampComponent::new
    );

    public static StreetLampComponent empty() {
        return new StreetLampComponent(
            StreetLampConfigCardItem.DEFAULT_TURN_ON_TIME,
            StreetLampConfigCardItem.DEFAULT_TURN_OFF_TIME,
            TimeFormat.TICKS
        );
    }
}
