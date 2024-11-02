package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RoadBuilderResetPacket extends BaseNetworkPacket<RoadBuilderResetPacket> {
    
    public RoadBuilderResetPacket() {}

    @Override
    public void encode(RoadBuilderResetPacket packet, RegistryFriendlyByteBuf buffer) {}

    @Override
    public RoadBuilderResetPacket decode(RegistryFriendlyByteBuf buffer) {
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
