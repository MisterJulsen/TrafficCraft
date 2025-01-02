package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RoadBuilderResetPacket implements IPacketBase<RoadBuilderResetPacket> {
    
    public RoadBuilderResetPacket() {}

    @Override
    public void encode(RoadBuilderResetPacket packet, FriendlyByteBuf buffer) {}

    @Override
    public RoadBuilderResetPacket decode(FriendlyByteBuf buffer) {
        return new RoadBuilderResetPacket();
    }
    
    @Override
    public void handle(RoadBuilderResetPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();

            if (sender.getMainHandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getMainHandItem());
            } else if (sender.getOffhandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getOffhandItem());
            }
            sender.getInventory().setChanged();
        });
    }
}
