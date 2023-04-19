package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PaintBrushPacket
{
    private int pattern;
    private float scroll;

    public PaintBrushPacket(int pattern, float scroll) {
        this.pattern = pattern;
        this.scroll = scroll;
    }

    public static void encode(PaintBrushPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.pattern);
        buffer.writeFloat(packet.scroll);
    }

    public static PaintBrushPacket decode(FriendlyByteBuf buffer) {
        int pattern = buffer.readInt();
        float scroll = buffer.readFloat();

        return new PaintBrushPacket(pattern, scroll);
    }

    public static void handle(PaintBrushPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();

            if(sender.getMainHandItem().getItem() instanceof BrushItem) {
                CompoundTag nbt = sender.getMainHandItem().getTag();
                nbt.putInt("pattern", packet.pattern);
                nbt.putFloat("scroll", packet.scroll);
            } else if (sender.getOffhandItem().getItem() instanceof BrushItem) {
             
                CompoundTag nbt = sender.getOffhandItem().getTag();
                nbt.putInt("pattern", packet.pattern);
                nbt.putFloat("scroll", packet.scroll);
            }
        });
        context.get().setPacketHandled(true);
    }
}
