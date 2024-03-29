package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.mcdragonlib.utils.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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

        return new StreetLampConfigPacket(turnOnTime, turnOffTime, TimeFormat.getFormatByIndex(timeFormat));
    }

    @Override
    public void handle(StreetLampConfigPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();

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

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
