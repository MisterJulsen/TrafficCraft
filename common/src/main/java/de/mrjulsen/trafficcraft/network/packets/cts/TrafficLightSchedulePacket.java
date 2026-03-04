package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.ArrayList;
import java.util.List;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class TrafficLightSchedulePacket extends NetworkPacketData {

    private static final String NBT_POS = "Pos";
    private static final String NBT_SCHEDULES = "Schedules";

    private BlockPos pos;
    private List<TrafficLightSchedule> schedules = new ArrayList<>();

    public TrafficLightSchedulePacket(DLStatus status) {
        super(status);
    }

    public TrafficLightSchedulePacket(BlockPos pos, List<TrafficLightSchedule> schedules) {
        super(DLStatus.OK);
        this.pos = pos;
        this.schedules = schedules;
    }

    @Override
    protected void write(CompoundTag nbt) {
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
        ListTag list = new ListTag();
        for (TrafficLightSchedule schedule : schedules) {
            list.add(schedule.toNbt());
        }
        nbt.put(NBT_SCHEDULES, list);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
        this.schedules = nbt.getList(NBT_SCHEDULES, Tag.TAG_COMPOUND).stream().map(x -> {
            TrafficLightSchedule schedule = new TrafficLightSchedule();
            schedule.fromNbt((CompoundTag)x);
            return schedule;
        }).toList();
    }
    
    public static void handle(TrafficLightSchedulePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer)context.getPlayer();
            if (player != null) {
                Level level = player.level();
                if (level.isLoaded(packet.pos)) {
                    if (level.getBlockEntity(packet.pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                        blockEntity.setSchedules(packet.schedules);
                    } else if (level.getBlockEntity(packet.pos) instanceof TrafficLightBlockEntity blockEntity) {
                        blockEntity.setSchedule(packet.schedules.get(0));
                    }
                    level.blockEntityChanged(packet.pos);
                }
            }
        });
    }
}
