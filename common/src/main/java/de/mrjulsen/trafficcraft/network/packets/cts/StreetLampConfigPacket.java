package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.mcdragonlib.util.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class StreetLampConfigPacket implements IPacketBase<StreetLampConfigPacket> {

    private int turnOnTime;
    private int turnOffTime;
    private TimeFormat timeFormat;

    public StreetLampConfigPacket() {}

    public StreetLampConfigPacket(int turnOnTime, int turnOffTime, TimeFormat timeFormat) {
        this.turnOnTime = turnOnTime;
        this.turnOffTime = turnOffTime;
        this.timeFormat = timeFormat;
    }

    @Override
    public void encode(StreetLampConfigPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.turnOnTime);
        buffer.writeInt(packet.turnOffTime);
        buffer.writeInt(packet.timeFormat.getIndex());
    }

    @Override
    public StreetLampConfigPacket decode(FriendlyByteBuf buffer) {
        int turnOnTime = buffer.readInt();
        int turnOffTime = buffer.readInt();
        int timeFormat = buffer.readInt();

        return new StreetLampConfigPacket(turnOnTime, turnOffTime, TimeFormat.getFormatByIndex((byte)timeFormat));
    }
    
    @Override
    public void handle(StreetLampConfigPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();

            if (sender.getMainHandItem().getItem() instanceof StreetLampConfigCardItem) {
                CompoundTag nbt = sender.getMainHandItem().getOrCreateTag();
                nbt.putInt("turnOnTime", packet.turnOnTime);
                nbt.putInt("turnOffTime", packet.turnOffTime);
                nbt.putInt("timeFormat", packet.timeFormat.getIndex());
            } else if (sender.getOffhandItem().getItem() instanceof StreetLampConfigCardItem) {             
                CompoundTag nbt = sender.getOffhandItem().getOrCreateTag();
                nbt.putInt("turnOnTime", packet.turnOnTime);
                nbt.putInt("turnOffTime", packet.turnOffTime);
                nbt.putInt("timeFormat", packet.timeFormat.getIndex());
            }
            
            sender.getInventory().setChanged();
        });
    }
}
