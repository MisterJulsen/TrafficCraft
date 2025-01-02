package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem.LinkerMode;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class LinkerModePacket implements IPacketBase<LinkerModePacket> {

    private LinkerMode mode;

    public LinkerModePacket() {}

    public LinkerModePacket(LinkerMode mode) {
        this.mode = mode;
    }

    @Override
    public void encode(LinkerModePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.mode);
    }

    @Override
    public LinkerModePacket decode(FriendlyByteBuf buffer) {
        LinkerMode mode = buffer.readEnum(LinkerMode.class); 
        return new LinkerModePacket(mode);
    }
    
    @Override
    public void handle(LinkerModePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();

            if (sender.getMainHandItem().getItem() instanceof TrafficLightLinkerItem) {
                TrafficLightLinkerItem.setMode(sender.getMainHandItem(), packet.mode);
            } else if (sender.getOffhandItem().getItem() instanceof TrafficLightLinkerItem) { 
                TrafficLightLinkerItem.setMode(sender.getOffhandItem(), packet.mode);
            }

            sender.getInventory().setChanged();
        });
    }
}
