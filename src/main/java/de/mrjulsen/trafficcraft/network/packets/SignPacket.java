package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.CircleTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.DiamondTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.MiscTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.block.RectangleTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.SquareTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.TriangleTrafficSignBlock;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import io.netty.handler.codec.mqtt.MqttProperties.IntegerProperty;
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
            BlockState state;
            switch (TrafficSignShape.getShapeByIndex(packet.shape)) {                
                case TRIANGLE:
                    state = ModBlocks.TRIANGLE_TRAFFIC_SIGN.get().defaultBlockState().setValue(TriangleTrafficSignBlock.TYPE, packet.pattern);
                    break;
                case SQUARE:
                    state = ModBlocks.SQUARE_TRAFFIC_SIGN.get().defaultBlockState().setValue(SquareTrafficSignBlock.TYPE, packet.pattern);
                    break;
                case DIAMOND:
                    state = ModBlocks.DIAMOND_TRAFFIC_SIGN.get().defaultBlockState().setValue(DiamondTrafficSignBlock.TYPE, packet.pattern);
                    break;
                case RECTANGLE:
                    state = ModBlocks.RECTANGLE_TRAFFIC_SIGN.get().defaultBlockState().setValue(RectangleTrafficSignBlock.TYPE, packet.pattern);

                    break;
                case MISC:
                    state = ModBlocks.MISC_TRAFFIC_SIGN.get().defaultBlockState().setValue(MiscTrafficSignBlock.TYPE, packet.pattern);
                    break;
                case CIRCLE:
                default:
                    state = ModBlocks.CIRCLE_TRAFFIC_SIGN.get().defaultBlockState().setValue(CircleTrafficSignBlock.TYPE, packet.pattern);
                    break;
            }
            BlockState oldState = sender.getLevel().getBlockState(packet.pos);
            sender.getLevel().setBlock(packet.pos, state.setValue(TrafficSignBlock.FACING, oldState.getValue(TrafficSignBlock.FACING)), 3);
        });
        context.get().setPacketHandled(true);
    }
}
