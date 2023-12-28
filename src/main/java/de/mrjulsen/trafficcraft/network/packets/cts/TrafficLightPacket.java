package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
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
    private Collection<TrafficLightColor> enabledColors;
    private TrafficLightType type;
    private TrafficLightModel model;
    private TrafficLightIcon icon;
    private TrafficLightControlType controlType;
    private TrafficLightColor[] colors;
    private int phaseId;
    private boolean scheduleEnabled;

    public TrafficLightPacket() {}

    public TrafficLightPacket(BlockPos pos, Collection<TrafficLightColor> enabledColors, TrafficLightType type, TrafficLightModel model, TrafficLightIcon icon, TrafficLightControlType controlType, TrafficLightColor[] colorSlots, int phaseId, boolean scheduleEnabled) {
        this.pos = pos;
        this.enabledColors = enabledColors;
        this.type = type;
        this.model = model;
        this.icon = icon;
        this.controlType = controlType;
        this.colors = colorSlots;
        this.phaseId = phaseId;
        this.scheduleEnabled = scheduleEnabled;
    }

    @Override
    public void encode(TrafficLightPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        TrafficLightColor[] enabledColorsArr = packet.enabledColors.toArray(TrafficLightColor[]::new);        
        buffer.writeBoolean(enabledColorsArr.length > 0);
        if (enabledColorsArr.length > 0) {
            byte[] enColBArr = new byte[enabledColorsArr.length];
            for (int i = 0; i < enabledColorsArr.length; i++) {
                enColBArr[i] = enabledColorsArr[i].getGroupIndex();
            }
            buffer.writeByteArray(enColBArr);
        }
        buffer.writeByte(packet.type.getIndex());
        buffer.writeByte(packet.model.getLightsCount());
        buffer.writeByte(packet.icon.getIndex());
        buffer.writeByte(packet.controlType.getIndex());
        buffer.writeBoolean(packet.colors.length > 0);
        if (packet.colors.length > 0) {
            byte[] colSlBArr = new byte[packet.colors.length];
            for (int i = 0; i < packet.colors.length; i++) {
                colSlBArr[i] = packet.colors[i].getGroupIndex();
            }
            buffer.writeByteArray(colSlBArr);
        }
        buffer.writeInt(packet.phaseId);
        buffer.writeBoolean(packet.scheduleEnabled);

    }

    @Override
    public TrafficLightPacket decode(FriendlyByteBuf buffer) {
        
        BlockPos pos = buffer.readBlockPos();
        Collection<TrafficLightColor> enabledColors = new ArrayList<>();
        byte[] enColBArr = new byte[0];
        if (buffer.readBoolean()) {
            enColBArr = buffer.readByteArray();
        }
        
        TrafficLightType type = TrafficLightType.getTypeByIndex(buffer.readByte());        
        for (byte b : enColBArr) {
            enabledColors.add(TrafficLightColor.getColorByGroupIndex(b, type));
        }
        TrafficLightModel model = TrafficLightModel.getModelByLightsCount(buffer.readByte());
        TrafficLightIcon icon = TrafficLightIcon.getIconByIndex(buffer.readByte());
        TrafficLightControlType controlType = TrafficLightControlType.getControlTypeByIndex(buffer.readByte());
        TrafficLightColor[] colorSlots = new TrafficLightColor[TrafficLightModel.maxRequiredSlots()];
        if (buffer.readBoolean()) {
            byte[] colSlBArr = buffer.readByteArray();
            for (int i = 0; i < colSlBArr.length && i < colorSlots.length; i++) {
                colorSlots[i] = TrafficLightColor.getColorByGroupIndex(colSlBArr[i], type);
            }
        }        
        int phaseId = buffer.readInt();
        boolean scheduleEnabled = buffer.readBoolean();

        return new TrafficLightPacket(pos, enabledColors, type, model, icon, controlType, colorSlots, phaseId, scheduleEnabled);
    }

    @Override
    public void handle(TrafficLightPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Level level = player.getLevel();
                if (level.isLoaded(packet.pos)) {
                    if (level.getBlockEntity(packet.pos) instanceof TrafficLightBlockEntity blockEntity) {
                        blockEntity.setRunning(packet.scheduleEnabled);
                        blockEntity.setPhaseId(packet.phaseId);
                        blockEntity.setControlType(packet.controlType);
                        blockEntity.setIcon(packet.icon);
                        blockEntity.setColorSlots(packet.colors);
                        blockEntity.enableOnlyColors(packet.enabledColors);
                        blockEntity.setType(packet.type);
                    }
                    BlockState state = level.getBlockState(packet.pos);
                    level.setBlockAndUpdate(packet.pos, state.setValue(TrafficLightBlock.MODEL, packet.model));
                }
            };
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
