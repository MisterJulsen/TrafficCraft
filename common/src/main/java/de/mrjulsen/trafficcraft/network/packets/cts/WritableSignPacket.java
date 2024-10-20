package de.mrjulsen.trafficcraft.network.packets.cts;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.block.WritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class WritableSignPacket implements IPacketBase<WritableSignPacket> {
    private String[] messages;
    private BlockPos pos;

    public WritableSignPacket() {}

    public WritableSignPacket(BlockPos pos, String[] messages) {
        this.pos = pos;
        this.messages = messages;
    }

    @Override
    public void encode(WritableSignPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.messages.length);
        for (int i = 0; i < packet.messages.length; i++) {
            String message = packet.messages[i];
            int messageLength = packet.messages[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(messageLength);
            buffer.writeUtf(message, messageLength);
        }
    }

    @Override
    public WritableSignPacket decode(FriendlyByteBuf buffer) {
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
    
    @Override
    public void handle(WritableSignPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.getLevel().getBlockEntity(packet.pos) instanceof WritableSignBlockEntity blockEntity) {
                blockEntity.setTexts(packet.messages);
            }
        });
    }
}
