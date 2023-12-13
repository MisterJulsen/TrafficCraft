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
    private float scroll;

    public PaintBrushPacket() {}

    public PaintBrushPacket(int pattern, float scroll) {
        this.pattern = pattern;
        this.scroll = scroll;
    }

    @Override
    public void encode(PaintBrushPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.pattern);
        buffer.writeFloat(packet.scroll);
    }

    @Override
    public PaintBrushPacket decode(FriendlyByteBuf buffer) {
        int pattern = buffer.readInt();
        float scroll = buffer.readFloat();

        return new PaintBrushPacket(pattern, scroll);
    }

    @Override
    public void handle(PaintBrushPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
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
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
