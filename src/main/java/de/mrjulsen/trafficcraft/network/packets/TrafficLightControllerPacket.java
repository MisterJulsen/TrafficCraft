package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightControllerPacket
{
    private BlockPos pos;
    private boolean status;


    public TrafficLightControllerPacket(BlockPos pos, boolean status)
    {
        this.pos = pos;
        this.status = status;
    }

    public static void encode(TrafficLightControllerPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.status);
    }

    public static TrafficLightControllerPacket decode(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        boolean status = buffer.readBoolean();

        return new TrafficLightControllerPacket(pos, status);
    }

    public static void handle(TrafficLightControllerPacket message, Supplier<NetworkEvent.Context> context)
    {
        ServerPlayer player = context.get().getSender();
        if(player != null)
        {
            Level level = player.getLevel();
            if(level.isLoaded(message.pos))
            {
                if (level.getBlockEntity(message.pos) instanceof TrafficLightControllerBlockEntity blockEntity)
                {
                    blockEntity.setRunning(message.status);
                }
            }
        }
        context.get().setPacketHandled(true);
    }
}
