package de.mrjulsen.trafficcraft.network.packets;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightPacket {

    private BlockPos pos;
    private int phaseId;
    private TrafficLightModel model;
    private TrafficLightIcon icon;
    private TrafficLightControlType controlType;
    private TrafficLightColor[] colorSlots;
    private boolean running;


    public TrafficLightPacket(BlockPos pos, int phaseId, TrafficLightModel model, TrafficLightIcon icon, TrafficLightControlType controlType, TrafficLightColor[] colorSlots, boolean running) {
        this.pos = pos;
        this.phaseId = phaseId;
        this.model = model;
        this.icon = icon;
        this.controlType = controlType;
        this.colorSlots = colorSlots;
        this.running = running;
    }

    public static void encode(TrafficLightPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.phaseId);
        buffer.writeEnum(packet.model);
        buffer.writeEnum(packet.icon);
        buffer.writeEnum(packet.controlType);
        buffer.writeVarIntArray(Arrays.stream(packet.colorSlots).mapToInt(x -> x.getIndex()).toArray());
        buffer.writeBoolean(packet.running);
    }

    public static TrafficLightPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int phaseId = buffer.readInt();
        TrafficLightModel model = buffer.readEnum(TrafficLightModel.class);
        TrafficLightIcon icon = buffer.readEnum(TrafficLightIcon.class);
        TrafficLightControlType controlType = buffer.readEnum(TrafficLightControlType.class);
        TrafficLightColor[] colors = Arrays.stream(buffer.readVarIntArray()).mapToObj(x -> TrafficLightColor.getDirectionByIndex(x)).toArray(TrafficLightColor[]::new);
        boolean running = buffer.readBoolean();

        return new TrafficLightPacket(pos, phaseId, model, icon, controlType, colors, running);
    }

    public static void handle(TrafficLightPacket message, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            Level level = player.getLevel();
            if (level.isLoaded(message.pos)) {
                if (level.getBlockEntity(message.pos) instanceof TrafficLightBlockEntity blockEntity) {
                    blockEntity.setRunning(message.running);
                    blockEntity.setPhaseId(message.phaseId);
                    blockEntity.setControlType(message.controlType);
                    blockEntity.setIcon(message.icon);
                    blockEntity.setColorSlots(message.colorSlots);                    
                }
                BlockState state = level.getBlockState(message.pos);
                level.setBlockAndUpdate(message.pos, state
                    .setValue(TrafficLightBlock.MODEL, message.model)
                );
            }
        }
        context.get().setPacketHandled(true);
    }
}
