package de.mrjulsen.trafficcraft.data;

import net.minecraft.nbt.CompoundTag;

public interface IClipboardData {
    CompoundTag serializeNbt();
    void deserializeNbt(CompoundTag nbt);
}
