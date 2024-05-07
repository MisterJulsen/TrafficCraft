package de.mrjulsen.legacydragonlib.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public interface IPacketBase<T> {
    void encode(T packet, FriendlyByteBuf buffer);
    T decode(FriendlyByteBuf buffer);
    void handle(T packet, Supplier<NetworkEvent.Context> supplier);
    NetworkDirection getDirection();
}
