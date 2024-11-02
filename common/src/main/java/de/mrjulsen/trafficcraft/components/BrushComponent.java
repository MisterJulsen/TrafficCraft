package de.mrjulsen.trafficcraft.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.data.PaintColor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BrushComponent(int patternId, int paintAmount, int colorId) {

    public static final Codec<BrushComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            Codec.INT.optionalFieldOf("pattern_id", 0).forGetter(BrushComponent::patternId),
            Codec.INT.optionalFieldOf("paint_amount", Constants.MAX_PAINT).forGetter(BrushComponent::paintAmount),
            Codec.INT.optionalFieldOf("color_id", PaintColor.WHITE.getIndex()).forGetter(BrushComponent::colorId)
        ).apply(builder, BrushComponent::new);
    });
    
    public static final StreamCodec<RegistryFriendlyByteBuf, BrushComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, BrushComponent::patternId,
        ByteBufCodecs.INT, BrushComponent::paintAmount,
        ByteBufCodecs.INT, BrushComponent::colorId,
        BrushComponent::new
    );

    public static BrushComponent empty() {
        return new BrushComponent(
            0, 
            0, 
            PaintColor.WHITE.getIndex()
        );
    }
}
