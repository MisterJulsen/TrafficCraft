package de.mrjulsen.trafficcraft.data.textures.data;

import com.mojang.serialization.Codec;
import de.mrjulsen.trafficcraft.data.textures.decoder.ITextureDecoder;

import java.util.function.Supplier;

public record TextureDataType<T extends ITextureData>(Codec<T> codec, Supplier<? extends ITextureDecoder<?>> decoder) {
}