package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.components.BrushComponent;
import de.mrjulsen.trafficcraft.item.BrushItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PaintBrushPacket extends BaseNetworkPacket<PaintBrushPacket> {

    private int pattern;

    public PaintBrushPacket() {}

    public PaintBrushPacket(int pattern) {
        this.pattern = pattern;
    }

    @Override
    public void encode(PaintBrushPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.pattern);
    }

    @Override
    public PaintBrushPacket decode(RegistryFriendlyByteBuf buffer) {
        int pattern = buffer.readInt();

        return new PaintBrushPacket(pattern);
    }
    
    @Override
    public void handle(PaintBrushPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            
            if (sender.getMainHandItem().getItem() instanceof BrushItem brush) {
                ItemStack stack = sender.getMainHandItem();
                BrushComponent comp = brush.getComponent(stack);
                brush.setComponent(stack, new BrushComponent(packet.pattern, comp.paintAmount(), comp.colorId()));
            } else if (sender.getOffhandItem().getItem() instanceof BrushItem brush) {
                ItemStack stack = sender.getOffhandItem();
                BrushComponent comp = brush.getComponent(stack);
                brush.setComponent(stack, new BrushComponent(packet.pattern, comp.paintAmount(), comp.colorId()));
            }
            sender.getInventory().setChanged();
        });
    }
}
