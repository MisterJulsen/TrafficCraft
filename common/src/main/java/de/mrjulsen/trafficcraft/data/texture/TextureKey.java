package de.mrjulsen.trafficcraft.data.texture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class TextureKey {

    private static final String NBT_TYPE_ID = "TypeId";
    private static final String NBT_TEXTURE_ID = "TextureId";
    private static final String NBT_SOURCE_TYPE = "SourceType";

    public enum SourceType {BUILT_IN, CUSTOM}

    private final ResourceLocation typeId;
    private final String textureId;
    private final SourceType sourceType;

    private TextureKey(ResourceLocation typeId, String textureId, SourceType sourceType) {
        this.typeId = typeId;
        this.textureId = textureId;
        this.sourceType = sourceType;
    }

    static TextureKey ofCustom(DynamicTextureRegistry.RegisteredTextureCodec<?> type, String textureId) {
        return new TextureKey(type.id(), textureId, SourceType.CUSTOM);
    }

    static TextureKey ofBuiltIn(DynamicTextureRegistry.RegisteredTextureCodec<?> type, ResourceLocation textureLocation) {
        return new TextureKey(type.id(), textureLocation.toString(), SourceType.BUILT_IN);
    }

    public static TextureKey fromNbt(CompoundTag nbt) {
        return new TextureKey(
                new ResourceLocation(nbt.getString(NBT_TYPE_ID)),
                nbt.getString(NBT_TEXTURE_ID),
                SourceType.valueOf(nbt.getString(NBT_SOURCE_TYPE))
        );
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(NBT_TYPE_ID, typeId.toString());
        nbt.putString(NBT_TEXTURE_ID, textureId);
        nbt.putString(NBT_SOURCE_TYPE, sourceType.name());
        return nbt;
    }

    public static TextureKey fromString(String encoded) {
        String[] parts = encoded.split("\\|", 3);
        return new TextureKey(
                new ResourceLocation(parts[0]),
                parts[2],
                SourceType.valueOf(parts[1])
        );
    }

    public ResourceLocation getTypeId() {
        return typeId;
    }

    public String getTextureId() {
        return textureId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public boolean isBuiltIn() {
        return sourceType == SourceType.BUILT_IN;
    }

    public ResourceLocation asBuiltInLocation() {
        if (!isBuiltIn()) throw new IllegalStateException("Not a built-in texture key.");
        return new ResourceLocation(textureId);
    }

    @Override
    public String toString() {
        return typeId + "|" + sourceType.name() + "|" + textureId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextureKey o) {
            return typeId.equals(o.typeId)
                    && textureId.equals(o.textureId)
                    && sourceType == o.sourceType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, textureId, sourceType);
    }
}