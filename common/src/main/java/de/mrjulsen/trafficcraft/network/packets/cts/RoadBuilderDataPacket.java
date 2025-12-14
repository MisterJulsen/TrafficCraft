package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class RoadBuilderDataPacket extends NetworkPacketData {

    private static final String NBT_REPLACE_BLOCKS = "ReplaceBlocks";
    private static final String NBT_ROAD_WIDTH = "RoadWidth";
    private static final String NBT_ROAD_TYPE = "RoadType";

    private boolean replaceBlocks;
    private byte roadWidth;
    private RoadType roadType;

    public RoadBuilderDataPacket(DLStatus status) {
        super(status);
    }
    
    public RoadBuilderDataPacket(boolean replaceBlocks, byte roadWidth, RoadType roadType) {
        super(DLStatus.OK);
        this.replaceBlocks = replaceBlocks;
        this.roadWidth = roadWidth;
        this.roadType = roadType;
    }    

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putBoolean(NBT_REPLACE_BLOCKS, replaceBlocks);
        nbt.putByte(NBT_ROAD_WIDTH, roadWidth);
        nbt.putInt(NBT_ROAD_TYPE, roadType.getIndex());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.replaceBlocks = nbt.getBoolean(NBT_REPLACE_BLOCKS);
        this.roadWidth = nbt.getByte(NBT_ROAD_WIDTH);
        this.roadType = RoadType.getRoadTypeByIndex(nbt.getInt(NBT_ROAD_TYPE));
    }
    
    public static void handle(RoadBuilderDataPacket packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.getMainHandItem().getItem() instanceof RoadConstructionTool) {
            CompoundTag nbt = sender.getMainHandItem().getOrCreateTag();
            nbt.putByte(RoadConstructionTool.NBT_ROAD_WIDTH, packet.roadWidth);
            nbt.putBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS, packet.replaceBlocks);
            nbt.putInt(RoadConstructionTool.NBT_ROAD_TYPE, packet.roadType.getIndex());
        } else if (sender.getOffhandItem().getItem() instanceof RoadConstructionTool) {             
            CompoundTag nbt = sender.getOffhandItem().getOrCreateTag();
            nbt.putByte(RoadConstructionTool.NBT_ROAD_WIDTH, packet.roadWidth);
            nbt.putBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS, packet.replaceBlocks);
            nbt.putInt(RoadConstructionTool.NBT_ROAD_TYPE, packet.roadType.getIndex());
        }
        sender.getInventory().setChanged();
    }
}
