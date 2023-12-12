package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.Arrays;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TrafficLightPacket implements IPacketBase<TrafficLightPacket> {

    private BlockPos pos;
    private int phaseId;
    private TrafficLightModel model;
    private TrafficLightIcon icon;
    private TrafficLightControlType controlType;
    private TrafficLightColor[] colorSlots;
    private boolean running;

    public TrafficLightPacket() {}

    public TrafficLightPacket(BlockPos pos, int phaseId, TrafficLightModel model, TrafficLightIcon icon, TrafficLightControlType controlType, TrafficLightColor[] colorSlots, boolean running) {
        this.pos = pos;
        this.phaseId = phaseId;
        this.model = model;
        this.icon = icon;
        this.controlType = controlType;
        this.colorSlots = colorSlots;
        this.running = running;
    }

    @Override
    public void encode(TrafficLightPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeInt(packet.phaseId);
        buffer.writeEnum(packet.model);
        buffer.writeEnum(packet.icon);
        buffer.writeEnum(packet.controlType);
        buffer.writeVarIntArray(Arrays.stream(packet.colorSlots).mapToInt(x -> x.getIndex()).toArray());
        buffer.writeBoolean(packet.running);
    }

    @Override
    public TrafficLightPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int phaseId = buffer.readInt();
        TrafficLightModel model = buffer.readEnum(TrafficLightModel.class);
        TrafficLightIcon icon = buffer.readEnum(TrafficLightIcon.class);
        TrafficLightControlType controlType = buffer.readEnum(TrafficLightControlType.class);
        TrafficLightColor[] colors = Arrays.stream(buffer.readVarIntArray()).mapToObj(x -> TrafficLightColor.getDirectionByIndex(x)).toArray(TrafficLightColor[]::new);
        boolean running = buffer.readBoolean();

        return new TrafficLightPacket(pos, phaseId, model, icon, controlType, colors, running);
    }

    @Override
    public void handle(TrafficLightPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Level level = player.getLevel();
                if (level.isLoaded(packet.pos)) {
                    if (level.getBlockEntity(packet.pos) instanceof TrafficLightBlockEntity blockEntity) {
                        blockEntity.setRunning(packet.running);
                        blockEntity.setPhaseId(packet.phaseId);
                        blockEntity.setControlType(packet.controlType);
                        blockEntity.setIcon(packet.icon);
                        blockEntity.setColorSlots(packet.colorSlots);                    
                    }
                    BlockState state = level.getBlockState(packet.pos);
                    level.setBlockAndUpdate(packet.pos, state
                        .setValue(TrafficLightBlock.MODEL, packet.model)
                    );
                }
            };
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
