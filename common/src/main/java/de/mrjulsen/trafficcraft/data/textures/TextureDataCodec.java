package de.mrjulsen.trafficcraft.data.textures;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TextureDataType;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class TextureDataCodec {

    public static final Codec<ITextureData> CODEC = new Codec<>() {

        private static final String TYPE_KEY = "type";

        @Override
        public <T> DataResult<Pair<ITextureData, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).flatMap(map -> {
                T typeValue = map.get(TYPE_KEY);
                if (typeValue == null)
                    return DataResult.error(() -> "Missing '" + TYPE_KEY + "' field");

                return ResourceLocation.CODEC.decode(ops, typeValue)
                        .flatMap(typePair -> {
                            ResourceLocation typeId = typePair.getFirst();
                            TextureDataType<?> type = lookupType(typeId);
                            if (type == null)
                                return DataResult.error(() -> "Unknown texture type: " + typeId);
                            return type.codec()
                                    .decode(ops, input)
                                    .map(p -> Pair.of(p.getFirst(), p.getSecond()));
                        });
            });
        }

        @Override
        public <T> DataResult<T> encode(ITextureData input, DynamicOps<T> ops, T prefix) {
            ResourceLocation typeId = lookupId(input.getType());
            if (typeId == null)
                return DataResult.error(() -> "Unregistered TextureDataType for: "
                        + input.getClass().getSimpleName());

            @SuppressWarnings("unchecked")
            Codec<ITextureData> typeCodec = (Codec<ITextureData>) input.getType().codec();
            return typeCodec.encode(input, ops, prefix)
                    .flatMap(encoded -> ops.mergeToMap(
                            encoded,
                            ops.createString(TYPE_KEY),
                            ops.createString(typeId.toString())
                    ));
        }
    };

    @Nullable
    public static TextureDataType<?> lookupType(ResourceLocation id) {
        return TextureDataTypes.TEXTURE_REGISTER.getRegistrar().get(id);
    }

    @Nullable
    public static ResourceLocation lookupId(TextureDataType<?> type) {
        return TextureDataTypes.TEXTURE_REGISTER.getRegistrar().getId(type);
    }
}