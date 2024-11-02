package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.util.accessor.DataAccessorType;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureManager;
import net.minecraft.resources.ResourceLocation;

public class ModAccessorTypes {
    
    public static final DataAccessorType<TrafficSignTextureData, Void, Void> CREATE_NEW_TRAFFIC_SIGN_TEXTURE = DataAccessorType.register(ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "create_new_traffic_sign_texture"), DataAccessorType.Builder.createEmptyResponse(
        (input, nbt) -> {
            nbt.put(DataAccessorType.DEFAULT_NBT_DATA, input.serializeNbt());
        }, (nbt) -> {
            return TrafficSignTextureData.deserializeNbt(nbt.getCompound(DataAccessorType.DEFAULT_NBT_DATA));
        }, (player, input, temp, nbt, iteration) -> {
            input.save();
            return false;
        }));

    public static final DataAccessorType<String, TrafficSignTextureData, TrafficSignTextureData> GET_TRAFFIC_SIGN_TEXTURE = DataAccessorType.register(ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, "get_traffic_sign_texture"), DataAccessorType.Builder.create(
        (input, nbt) -> {
            nbt.putString(DataAccessorType.DEFAULT_NBT_DATA, input);
        }, (nbt) -> {
            return nbt.getString(DataAccessorType.DEFAULT_NBT_DATA);
        }, (player, input, temp, nbt, iteration) -> {
            nbt.put(DataAccessorType.DEFAULT_NBT_DATA, TrafficSignTextureManager.load(input).serializeNbt());
            return false;
        }, (hasMore, temp, iteration, nbt) -> {
            return TrafficSignTextureData.deserializeNbt(nbt.getCompound(DataAccessorType.DEFAULT_NBT_DATA));
        }));

    public static void init() {}
}
