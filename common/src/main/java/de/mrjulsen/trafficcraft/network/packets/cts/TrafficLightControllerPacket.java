package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class TrafficLightControllerPacket extends NetworkPacketData {

    private static final String NBT_POS = "Pos";
    private static final String NBT_STATUS = "Status";

    private BlockPos pos;
    private boolean status;

    public TrafficLightControllerPacket(DLStatus status) {
        super(status);
    }

    public TrafficLightControllerPacket(BlockPos pos, boolean status) {
        super(DLStatus.OK);
        this.pos = pos;
        this.status = status;
    }

    @Override
    protected void write(CompoundTag nbt) {
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
        nbt.putBoolean(NBT_STATUS, status);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
        this.status = nbt.getBoolean(NBT_STATUS);
    }

    public static void handle(TrafficLightControllerPacket packet, NetworkPacketContext context) {        
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        if (player != null) {
            Level level = player.level();
            if (level.isLoaded(packet.pos)) {
                if (level.getBlockEntity(packet.pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                    blockEntity.setRunning(packet.status);
                }
            }
        }
    }
}
