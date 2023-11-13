package de.mrjulsen.trafficcraft.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.TownSignBlock.ETownSignSide;
import de.mrjulsen.trafficcraft.block.data.TownSignVariant;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class TownSignPacket {
    private String[] messages;
    private TownSignVariant variant;
    private BlockPos pos;    
    private TownSignBlock.ETownSignSide side;

    public TownSignPacket(BlockPos pos, String[] messages, TownSignVariant variant, TownSignBlock.ETownSignSide side)
    {
        this.pos = pos;
        this.variant = variant;
        this.messages = messages;
        this.side = side;
    }

    public static void encode(TownSignPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.variant.getIndex());
        buffer.writeInt(packet.side.getIndex());
        buffer.writeInt(packet.messages.length);
        for (int i = 0; i < packet.messages.length; i++) {
            String message = packet.messages[i];
            int messageLength = packet.messages[i].getBytes(StandardCharsets.UTF_8).length;
            buffer.writeInt(messageLength);
            buffer.writeUtf(message, messageLength);
        }
    }

    public static TownSignPacket decode(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        TownSignVariant variant = TownSignVariant.getVariantByIndex(buffer.readInt());
        TownSignBlock.ETownSignSide side = ETownSignSide.getSideByIndex(buffer.readInt());
        int messagesCount = buffer.readInt();
        String[] messages = new String[messagesCount];
        for (int i = 0; i < messagesCount; i++) {
            int messageLength = buffer.readInt();
            messages[i] = buffer.readUtf(messageLength);
        }

        TownSignPacket instance = new TownSignPacket(pos, messages, variant, side);
        return instance;
    }

    public static void handle(TownSignPacket packet, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer sender = context.get().getSender();
            if (sender.getLevel().getBlockEntity(packet.pos) instanceof WritableTrafficSignBlockEntity blockEntity) {
                
            }

            if (sender.getLevel().getBlockState(packet.pos).getBlock() instanceof TownSignBlock && sender.getLevel().getBlockEntity(packet.pos) instanceof TownSignBlockEntity blockEntity) {
                switch (packet.side) {
                    case BACK:
                        blockEntity.setBackTexts(packet.messages);
                        break;
                    default:
                    case FRONT:
                        blockEntity.setTexts(packet.messages);
                        break;
                }
                BlockState state = sender.getLevel().getBlockState(packet.pos);
                sender.getLevel().setBlockAndUpdate(packet.pos, state.setValue(TownSignBlock.VARIANT, packet.variant));
            }
            
        });
        context.get().setPacketHandled(true);
    }
}
