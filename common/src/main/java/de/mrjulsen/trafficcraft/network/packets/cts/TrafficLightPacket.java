package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.Arrays;
import java.util.Collection;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightPacket extends NetworkPacketData {

    private static final String NBT_POS = "Pos";
    private static final String NBT_ENABLED_COLORS = "EnabledColors";
    private static final String NBT_TYPE = "Type";
    private static final String NBT_MODEL = "Model";
    private static final String NBT_ICON = "Icon";
    private static final String NBT_CONTROL_TYPE = "ControlType";
    private static final String NBT_COLORS = "Colors";
    private static final String NBT_ID = "Id";
    private static final String NBT_SCHEDULED = "IsScheduled";

    private BlockPos pos;
    private Collection<TrafficLightColor> enabledColors;
    private TrafficLightType type;
    private TrafficLightModel model;
    private TrafficLightIcon icon;
    private TrafficLightControlType controlType;
    private TrafficLightColor[] colors;
    private int phaseId;
    private boolean scheduleEnabled;

    public TrafficLightPacket(DLStatus status) {
        super(status);
    }

    public TrafficLightPacket(BlockPos pos, Collection<TrafficLightColor> enabledColors, TrafficLightType type, TrafficLightModel model, TrafficLightIcon icon, TrafficLightControlType controlType, TrafficLightColor[] colorSlots, int phaseId, boolean scheduleEnabled) {
        super(DLStatus.OK);
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
    protected void write(CompoundTag nbt) {
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
        ListTag enabledColorsList = new ListTag();
        for (TrafficLightColor color : enabledColors) {
            enabledColorsList.add(ByteTag.valueOf(color.getGroupIndex()));
        }
        nbt.put(NBT_ENABLED_COLORS, enabledColorsList);
        nbt.putByte(NBT_TYPE, type.getIndex());
        nbt.putByte(NBT_MODEL, model.getLightsCount());
        nbt.putByte(NBT_ICON, icon.getIndex());
        nbt.putByte(NBT_CONTROL_TYPE, controlType.getIndex());
        ListTag colorsList = new ListTag();
        for (TrafficLightColor color : colors) {
            colorsList.add(ByteTag.valueOf(color.getGroupIndex()));
        }
        nbt.put(NBT_COLORS, colorsList);
        nbt.putInt(NBT_ID, phaseId);
        nbt.putBoolean(NBT_SCHEDULED, scheduleEnabled);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
        this.type = TrafficLightType.getTypeByIndex(nbt.getByte(NBT_TYPE));
        this.enabledColors = nbt.getList(NBT_ENABLED_COLORS, Tag.TAG_BYTE).stream().map(x -> TrafficLightColor.getColorByGroupIndex(((ByteTag)x).getAsByte(), type)).toList();
        this.model = TrafficLightModel.getModelByLightsCount(nbt.getByte(NBT_MODEL));
        this.icon = TrafficLightIcon.getIconByIndex(nbt.getByte(NBT_ICON));
        this.controlType = TrafficLightControlType.getControlTypeByIndex(nbt.getByte(NBT_CONTROL_TYPE));
        this.colors = nbt.getList(NBT_COLORS, Tag.TAG_BYTE).stream().map(x -> TrafficLightColor.getColorByGroupIndex(((ByteTag)x).getAsByte(), type)).toArray(TrafficLightColor[]::new);
        this.phaseId = nbt.getInt(NBT_ID);
        this.scheduleEnabled = nbt.getBoolean(NBT_SCHEDULED);
    }
    
    
    public static void handle(TrafficLightPacket packet, NetworkPacketContext context) {
        ServerPlayer player = (ServerPlayer)context.getPlayer();
        if (player != null) {
            Level level = player.level();
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
                level.blockEntityChanged(packet.pos);
            }
        };
    }
}
