package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.components.RoadConstructionToolComponent;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();
            ItemStack stack;

            if ((stack = sender.getMainHandItem()).getItem() instanceof RoadConstructionTool item) {
                RoadConstructionToolComponent comp = item.getComponent(stack);
                item.setComponent(stack, new RoadConstructionToolComponent(comp.start(), comp.end(), packet.roadType, packet.roadWidth, packet.replaceBlocks));
            } else if ((stack = sender.getOffhandItem()).getItem() instanceof RoadConstructionTool item) {
                RoadConstructionToolComponent comp = item.getComponent(stack);
                item.setComponent(stack, new RoadConstructionToolComponent(comp.start(), comp.end(), packet.roadType, packet.roadWidth, packet.replaceBlocks));
            }
            sender.getInventory().setChanged();
        });
    }
}
