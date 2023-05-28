package de.mrjulsen.trafficcraft.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class WritableSignPacket {
    private String[] messages;
    private BlockPos pos;

    public WritableSignPacket(BlockPos pos, String[] messages)
    {
        this.pos = pos;
        this.messages = messages;
    }

    public static void encode(WritableSignPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.messages.length);
        for (int i = 0; i < packet.messages.length; i++) {
            String message = packet.messages[i];
            int messageLength = packet.messages[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(messageLength);
            buffer.writeUtf(message, messageLength);
        }
    }

    public static WritableSignPacket decode(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        int messagesCount = buffer.readInt();
        String[] messages = new String[messagesCount];
        for (int i = 0; i < messagesCount; i++) {
            int messageLength = buffer.readInt();
            messages[i] = buffer.readUtf(messageLength);
        }

        WritableSignPacket instance = new WritableSignPacket(pos, messages);
        return instance;
    }

    public static void handle(WritableSignPacket packet, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer sender = context.get().getSender();
            if (sender.getLevel().getBlockEntity(packet.pos) instanceof WritableTrafficSignBlockEntity blockEntity) {
                blockEntity.setTexts(packet.messages);
            }
            
        });
        context.get().setPacketHandled(true);
    }
}
