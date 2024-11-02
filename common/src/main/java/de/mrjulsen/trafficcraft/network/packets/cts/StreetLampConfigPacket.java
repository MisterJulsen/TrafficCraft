package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.util.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.components.StreetLampComponent;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StreetLampConfigPacket extends BaseNetworkPacket<StreetLampConfigPacket> {

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
    public void encode(StreetLampConfigPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.turnOnTime);
        buffer.writeInt(packet.turnOffTime);
        buffer.writeInt(packet.timeFormat.getIndex());
    }

    @Override
    public StreetLampConfigPacket decode(RegistryFriendlyByteBuf buffer) {
        int turnOnTime = buffer.readInt();
        int turnOffTime = buffer.readInt();
        int timeFormat = buffer.readInt();

        return new StreetLampConfigPacket(turnOnTime, turnOffTime, TimeFormat.getFormatByIndex((byte)timeFormat));
    }
    
    @Override
    public void handle(StreetLampConfigPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            ItemStack stack;

            if ((stack = sender.getMainHandItem()).getItem() instanceof StreetLampConfigCardItem item) {
                item.setComponent(stack, new StreetLampComponent(packet.turnOnTime, packet.turnOffTime, packet.timeFormat));
            } else if ((stack = sender.getOffhandItem()).getItem() instanceof StreetLampConfigCardItem item) {   
                item.setComponent(stack, new StreetLampComponent(packet.turnOnTime, packet.turnOffTime, packet.timeFormat));
            }
            
            sender.getInventory().setChanged();
        });
    }
}
