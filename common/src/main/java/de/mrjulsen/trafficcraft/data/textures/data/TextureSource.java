package de.mrjulsen.trafficcraft.data.textures.data;

import net.minecraft.resources.ResourceLocation;

public sealed interface TextureSource permits TextureSource.BuiltIn, TextureSource.Custom {

    enum Type {
        BUILT_IN, CUSTOM;
    }

    record BuiltIn(ResourceLocation location) implements TextureSource {}
    record Custom(byte[] bytes, int width, int height) implements TextureSource {}
}