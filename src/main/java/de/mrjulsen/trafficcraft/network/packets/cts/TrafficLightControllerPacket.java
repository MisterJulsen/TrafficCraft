package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
    public void handle(TrafficLightControllerPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer player = context.get().getSender();
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

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
