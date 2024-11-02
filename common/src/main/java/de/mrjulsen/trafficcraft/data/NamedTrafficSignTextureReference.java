package de.mrjulsen.trafficcraft.data;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class NamedTrafficSignTextureReference {

    public static final MapCodec<NamedTrafficSignTextureReference> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
            Codec.STRING.fieldOf("texture_id").forGetter(NamedTrafficSignTextureReference::getTextureId),
            Codec.STRING.fieldOf("name").forGetter(NamedTrafficSignTextureReference::getName)
        ).apply(instance, NamedTrafficSignTextureReference::new);
    });

    private final String textureId;
    private final String name;

    private NamedTrafficSignTextureReference(String textureId, String name) {
        this.textureId = textureId;
        this.name = name;
    }

    public String getTextureId() {
        return textureId;
    }

    public String getName() {
        return name;
    }
    
    public static NamedTrafficSignTextureReference fromNetwork(RegistryFriendlyByteBuf buffer) {
        String textureId = buffer.readUtf();
        String name = buffer.readUtf();
        return new NamedTrafficSignTextureReference(textureId, name);
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, NamedTrafficSignTextureReference ref) {
        buffer.writeUtf(ref.getTextureId());
        buffer.writeUtf(ref.getName());
    }

    public static NamedTrafficSignTextureReference of(TrafficSignTextureData data, String name) {
        return new NamedTrafficSignTextureReference(data.getHash().toString(), name);
    }

    public static NamedTrafficSignTextureReference ofBuildIn(String name, BuildInTrafficSignCodec codec) {
        return new NamedTrafficSignTextureReference(codec.encode(), name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedTrafficSignTextureReference o) {
            return getTextureId().equals(o.getTextureId()) && getName().equals(o.getName());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTextureId(), getName());
    }

    @Override
    public String toString() {
        return getName();
    }

    public static final record BuildInTrafficSignCodec(TrafficSignShape shape, int id, short width, short height) {

        public static final String PREFIX = "builtIn";

        public String encode() {
            return String.format("%s_%s_%s@%sx%s", PREFIX, shape().getIndex(), id(), width(), height());
        }

        public static BuildInTrafficSignCodec decode(String code) {
            try {
                String[] parts = code.split("@");
                String[] locationData = parts[0].split("_");
                String[] sizeData = parts.length > 1 ? parts[1].split("x") : new String[0]; 
                int shapeIndex = Integer.parseInt(locationData[1]);
                int id = Integer.parseInt(locationData[2]);
                short width = sizeData.length > 0 ? Short.parseShort(sizeData[0]) : 32;
                short height = sizeData.length > 0 ? Short.parseShort(sizeData[1]) : 32;
                return new BuildInTrafficSignCodec(TrafficSignShape.getShapeByIndex(shapeIndex), id, width, height);
            } catch (Exception e) {
                TrafficCraft.LOGGER.warn("Unable to decode traffic sign texture location.", e);
            }
            return empty();
        }

        public static BuildInTrafficSignCodec empty() {
            return new BuildInTrafficSignCodec(TrafficSignShape.CIRCLE, 0, (short)32, (short)32);
        }

        @Override
        public final String toString() {
            return encode();
        }
    }
}
