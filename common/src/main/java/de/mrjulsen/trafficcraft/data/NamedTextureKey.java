package de.mrjulsen.trafficcraft.data;

import de.mrjulsen.trafficcraft.data.textures.TextureIdentifier;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record NamedTextureKey(TextureIdentifier textureKey, String name) {

    private static final String NBT_NAME = "Name";
    private static final String NBT_ID = "TextureKey";

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.put(NBT_ID, textureKey.toNbt());
        nbt.putString(NBT_NAME, name);
        return nbt;
    }

    public static NamedTextureKey fromNbt(CompoundTag nbt) {
        return new NamedTextureKey(
                TextureIdentifier.fromNbt(nbt.getCompound(NBT_ID)),
                nbt.getString(NBT_NAME)
        );
    }

    @Override
    public @NotNull String toString() {
        return name();
    }
}
