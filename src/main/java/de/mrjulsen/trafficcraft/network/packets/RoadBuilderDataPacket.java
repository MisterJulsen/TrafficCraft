package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RoadBuilderDataPacket {

    private boolean replaceBlocks;
    private byte roadWidth;
    private RoadType roadType;
    
    public RoadBuilderDataPacket(boolean replaceBlocks, byte roadWidth, RoadType roadType) {
        this.replaceBlocks = replaceBlocks;
        this.roadWidth = roadWidth;
        this.roadType = roadType;
    }

    public static void encode(RoadBuilderDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeByte(packet.roadWidth);
        buffer.writeEnum(packet.roadType);
    }

    public static RoadBuilderDataPacket decode(FriendlyByteBuf buffer) {
        boolean replaceBlocks = buffer.readBoolean();
        byte roadWidth = buffer.readByte();
        RoadType roadType = buffer.readEnum(RoadType.class);
        return new RoadBuilderDataPacket(replaceBlocks, roadWidth, roadType);
    }

    public static void handle(RoadBuilderDataPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();

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
        });
        context.get().setPacketHandled(true);
    }
}
