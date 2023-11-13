package de.mrjulsen.trafficcraft.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightSchedulePacket
{
    private BlockPos pos;
    private List<TrafficLightSchedule> schedules = new ArrayList<>();


    public TrafficLightSchedulePacket(BlockPos pos, List<TrafficLightSchedule> schedules)
    {
        this.pos = pos;
        this.schedules = schedules;
    }

    public static void encode(TrafficLightSchedulePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.schedules.size());
        for (TrafficLightSchedule schedule : packet.schedules) {
            schedule.toBytes(buffer);
        }
    }

    public static TrafficLightSchedulePacket decode(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readInt();
        List<TrafficLightSchedule> schedules = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            schedules.add(TrafficLightSchedule.fromBytes(buffer));
        }

        return new TrafficLightSchedulePacket(pos, schedules);
    }

    public static void handle(TrafficLightSchedulePacket message, Supplier<NetworkEvent.Context> context)
    {
        ServerPlayer player = context.get().getSender();
        if(player != null)
        {
            Level level = player.getLevel();
            if(level.isLoaded(message.pos))
            {
                if (level.getBlockEntity(message.pos) instanceof TrafficLightControllerBlockEntity blockEntity)
                {
                    blockEntity.setSchedules(message.schedules);
                } else if (level.getBlockEntity(message.pos) instanceof TrafficLightBlockEntity blockEntity)
                {
                    blockEntity.setSchedule(message.schedules.get(0));
                }
            }
        }
        context.get().setPacketHandled(true);
    }
}
