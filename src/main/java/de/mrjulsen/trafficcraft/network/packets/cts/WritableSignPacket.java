package de.mrjulsen.trafficcraft.network.packets.cts;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
    public void handle(WritableSignPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.level().getBlockEntity(packet.pos) instanceof WritableTrafficSignBlockEntity blockEntity) {
                blockEntity.setTexts(packet.messages);
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
