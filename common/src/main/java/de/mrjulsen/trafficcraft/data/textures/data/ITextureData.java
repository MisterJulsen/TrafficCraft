package de.mrjulsen.trafficcraft.data.textures.data;

import de.mrjulsen.trafficcraft.data.textures.TextureHandleExtension;

import java.util.Optional;

public interface ITextureData {
    TextureDataType<? extends ITextureData> getType();
    TextureSource getSource();

    default Optional<TextureHandleExtension> createExtension() {
        return Optional.empty();
    }

    static <T extends ITextureData> Optional<T> ifType(Class<T> type, ITextureData instance) {
        ITextureData data = instance;
        if (data instanceof NbtTextureData nbt) {
            data = nbt.getInner();
        }
        return type.isInstance(data) ? Optional.of(type.cast(data)) : Optional.empty();
    }
}