package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
    public void handle(PaintBrushPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();

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

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
