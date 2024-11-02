package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.components.TrafficLightLinkerComponent;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem.LinkerMode;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LinkerModePacket extends BaseNetworkPacket<LinkerModePacket> {

    private LinkerMode mode;

    public LinkerModePacket() {}

    public LinkerModePacket(LinkerMode mode) {
        this.mode = mode;
    }

    @Override
    public void encode(LinkerModePacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(packet.mode);
    }

    @Override
    public LinkerModePacket decode(RegistryFriendlyByteBuf buffer) {
        LinkerMode mode = buffer.readEnum(LinkerMode.class); 
        return new LinkerModePacket(mode);
    }
    
    @Override
    public void handle(LinkerModePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            ItemStack stack;

            if ((stack = sender.getMainHandItem()).getItem() instanceof TrafficLightLinkerItem item) {
                TrafficLightLinkerComponent comp = item.getComponent(stack);
                item.setComponent(stack, new TrafficLightLinkerComponent(comp.location(), mode, comp.targetBlockName()));
            } else if ((stack = sender.getOffhandItem()).getItem() instanceof TrafficLightLinkerItem item) { 
                TrafficLightLinkerComponent comp = item.getComponent(stack);
                item.setComponent(stack, new TrafficLightLinkerComponent(comp.location(), mode, comp.targetBlockName()));
            }

            sender.getInventory().setChanged();
        });
    }
}
