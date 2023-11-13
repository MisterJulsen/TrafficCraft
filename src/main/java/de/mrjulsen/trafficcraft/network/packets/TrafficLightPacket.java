package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightDirection;
import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.block.data.TrafficLightVariant;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightPacket
{
    private BlockPos pos;
    private int phaseId;
    private int mode;
    private int variant;
    private int direction;
    private int controlType;
    private boolean running;


    public TrafficLightPacket(BlockPos pos, int phaseId, int mode, int variant, int direction, int controlType, boolean running)
    {
        this.pos = pos;
        this.phaseId = phaseId;
        this.mode = mode;
        this.variant = variant;
        this.direction = direction;
        this.controlType = controlType;
        this.running = running;
    }

    public static void encode(TrafficLightPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.phaseId);
        buffer.writeInt(packet.mode);
        buffer.writeInt(packet.variant);
        buffer.writeInt(packet.direction);
        buffer.writeInt(packet.controlType);
        buffer.writeBoolean(packet.running);
    }

    public static TrafficLightPacket decode(FriendlyByteBuf buffer)
    {
        BlockPos pos = buffer.readBlockPos();
        int phaseId = buffer.readInt();
        int mode = buffer.readInt();
        int variant = buffer.readInt();
        int direction = buffer.readInt();
        int controlType = buffer.readInt();
        boolean running = buffer.readBoolean();

        return new TrafficLightPacket(pos, phaseId, mode, variant, direction, controlType, running);
    }

    public static void handle(TrafficLightPacket message, Supplier<NetworkEvent.Context> context)
    {
        ServerPlayer player = context.get().getSender();
        if(player != null)
        {
            Level level = player.getLevel();
            if(level.isLoaded(message.pos))
            {
                if(level.getBlockEntity(message.pos) instanceof TrafficLightBlockEntity blockEntity)
                {
                    blockEntity.setRunning(message.running);
                    blockEntity.setPhaseId(message.phaseId);
                    blockEntity.setControlType(message.controlType);
                }
                BlockState state = level.getBlockState(message.pos);
                level.setBlockAndUpdate(message.pos, state
                    .setValue(TrafficLightBlock.MODE, TrafficLightMode.getModeByIndex(message.mode))
                    .setValue(TrafficLightBlock.VARIANT, TrafficLightVariant.getVariantByIndex(message.variant))
                    .setValue(TrafficLightBlock.DIRECTION, TrafficLightDirection.getDirectionByIndex(message.direction))
                );
            }
        }
        context.get().setPacketHandled(true);
    }
}
