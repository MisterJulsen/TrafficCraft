package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class SignPacket {
    private int pattern;
    private int shape;
    private BlockPos pos;
    private float scroll;

    public SignPacket(int pattern, int shape, float scroll, BlockPos pos)
    {
        this.pattern = pattern;
        this.scroll = scroll;
        this.pos = pos;
        this.shape = shape;
    }

    public static void encode(SignPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeInt(packet.pattern);
        buffer.writeInt(packet.shape);
        buffer.writeFloat(packet.scroll);
        buffer.writeBlockPos(packet.pos);
    }

    public static SignPacket decode(FriendlyByteBuf buffer)
    {
        int pattern = buffer.readInt();
        int shape = buffer.readInt();
        float scroll = buffer.readFloat();
        BlockPos pos = buffer.readBlockPos();

        SignPacket instance = new SignPacket(pattern, shape, scroll, pos);
        return instance;
    }

    public static void handle(SignPacket packet, Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayer sender = context.get().getSender();
            Level level = sender.getLevel();

            BlockState state = sender.getLevel().getBlockState(packet.pos);
            sender.getLevel().setBlock(packet.pos, state.setValue(TrafficSignBlock.TYPE, packet.pattern).setValue(TrafficSignBlock.SHAPE, TrafficSignShape.getShapeByIndex(packet.shape)), 3);
        });
        context.get().setPacketHandled(true);
    }
}
