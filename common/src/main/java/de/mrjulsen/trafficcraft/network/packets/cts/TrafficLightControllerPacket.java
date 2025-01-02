package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class TrafficLightControllerPacket implements IPacketBase<TrafficLightControllerPacket> {
    private BlockPos pos;
    private boolean status;

    public TrafficLightControllerPacket() {}

    public TrafficLightControllerPacket(BlockPos pos, boolean status) {
        this.pos = pos;
        this.status = status;
    }

    @Override
    public void encode(TrafficLightControllerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.status);
    }

    @Override
    public TrafficLightControllerPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        boolean status = buffer.readBoolean();

        return new TrafficLightControllerPacket(pos, status);
    }
    
    @Override
    public void handle(TrafficLightControllerPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer player = (ServerPlayer)contextSupplier.get().getPlayer();
            if (player != null) {
                Level level = player.getLevel();
                if (level.isLoaded(packet.pos)) {
                    if (level.getBlockEntity(packet.pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                        blockEntity.setRunning(packet.status);
                    }
                }
            }
        });
    }
}
