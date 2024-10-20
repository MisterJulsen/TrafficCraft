package de.mrjulsen.trafficcraft.network.packets.stc;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficSignTextureResetPacket implements IPacketBase<TrafficSignTextureResetPacket> {
    public BlockPos pos;

    public TrafficSignTextureResetPacket() {}

    public TrafficSignTextureResetPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(TrafficSignTextureResetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    @Override
    public TrafficSignTextureResetPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();

        return new TrafficSignTextureResetPacket(pos);
    }
    
    @Override
    public void handle(TrafficSignTextureResetPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {                
                Player player = contextSupplier.get().getPlayer();                
                Level level = player.level;
                BlockEntity entity = level.getBlockEntity(packet.pos);
                if (entity instanceof TrafficSignBlockEntity be) {
                    be.resetTexture();
                }
            });
        });
    }
}
