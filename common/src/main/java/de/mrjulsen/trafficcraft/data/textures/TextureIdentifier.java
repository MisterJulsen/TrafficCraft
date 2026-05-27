package de.mrjulsen.trafficcraft.data.textures;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public final class TextureIdentifier {

    private static final String NBT_TYPE = "Type";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_IS_CUSTOM = "IsCustom";

    private final ResourceLocation typeId;
    private final String location;
    private final boolean isCustom;

    private TextureIdentifier(ResourceLocation typeId, String location, boolean isCustom) {
        this.typeId = typeId;
        this.location = location;
        this.isCustom = isCustom;
    }


    public static TextureIdentifier builtIn(ResourceLocation textureDataTypeId, ResourceLocation jsonPath) {
        return new TextureIdentifier(textureDataTypeId, jsonPath.toString(), false);
    }

    public static TextureIdentifier custom(ResourceLocation textureDataTypeId, UUID uuid) {
        return new TextureIdentifier(textureDataTypeId, uuid.toString(), true);
    }

    public ResourceLocation getTypeId() {
        return typeId;
    }

    public String getLocation() {
        return location;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_TYPE, typeId.toString());
        tag.putString(NBT_LOCATION, location);
        tag.putBoolean(NBT_IS_CUSTOM, isCustom);
        return tag;
    }

    public static TextureIdentifier fromNbt(CompoundTag tag) {
        return new TextureIdentifier(
                DLUtils.resourceLocation(tag.getString(NBT_TYPE)),
                tag.getString(NBT_LOCATION),
                tag.getBoolean(NBT_IS_CUSTOM)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextureIdentifier o) {
            return typeId.equals(o.typeId) && location.equals(o.location) && isCustom == o.isCustom;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, location, isCustom);
    }

    @Override
    public String toString() {
        return (isCustom ? "custom" : "builtin") + ":" + typeId + "/" + location;
    }
}
