package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.item.BrushItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class PaintBrushPacket implements IPacketBase<PaintBrushPacket> {

    private int pattern;

    public PaintBrushPacket() {}

    public PaintBrushPacket(int pattern) {
        this.pattern = pattern;
    }

    @Override
    public void encode(PaintBrushPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.pattern);
    }

    @Override
    public PaintBrushPacket decode(FriendlyByteBuf buffer) {
        int pattern = buffer.readInt();

        return new PaintBrushPacket(pattern);
    }
    
    @Override
    public void handle(PaintBrushPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            Player sender = contextSupplier.get().getPlayer();

            if(sender.getMainHandItem().getItem() instanceof BrushItem) {
                CompoundTag nbt = sender.getMainHandItem().getTag();
                nbt.putInt(BrushItem.NBT_PATTERN, packet.pattern);
            } else if (sender.getOffhandItem().getItem() instanceof BrushItem) {
             
                CompoundTag nbt = sender.getOffhandItem().getTag();
                nbt.putInt(BrushItem.NBT_PATTERN, packet.pattern);
            }
            sender.getInventory().setChanged();
        });
    }
}
