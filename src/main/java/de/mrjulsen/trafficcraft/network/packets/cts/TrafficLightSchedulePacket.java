package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightSchedulePacket implements IPacketBase<TrafficLightSchedulePacket> {
    private BlockPos pos;
    private List<TrafficLightSchedule> schedules = new ArrayList<>();

    public TrafficLightSchedulePacket() {}

    public TrafficLightSchedulePacket(BlockPos pos, List<TrafficLightSchedule> schedules) {
        this.pos = pos;
        this.schedules = schedules;
    }

    @Override
    public void encode(TrafficLightSchedulePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.schedules.size());
        for (TrafficLightSchedule schedule : packet.schedules) {
            schedule.toBytes(buffer);
        }
    }

    @Override
    public TrafficLightSchedulePacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readInt();
        List<TrafficLightSchedule> schedules = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            schedules.add(TrafficLightSchedule.fromBytes(buffer));
        }

        return new TrafficLightSchedulePacket(pos, schedules);
    }

    @Override
    public void handle(TrafficLightSchedulePacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer player = context.get().getSender();
            if(player != null) {
                Level level = player.getLevel();
                if(level.isLoaded(packet.pos)) {
                    if (level.getBlockEntity(packet.pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                        blockEntity.setSchedules(packet.schedules);
                    } else if (level.getBlockEntity(packet.pos) instanceof TrafficLightBlockEntity blockEntity) {
                        blockEntity.setSchedule(packet.schedules.get(0));
                    }
                }
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
