package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RoadBuilderDataPacket implements IPacketBase<RoadBuilderDataPacket> {

    private boolean replaceBlocks;
    private byte roadWidth;
    private RoadType roadType;

    public RoadBuilderDataPacket() {}
    
    public RoadBuilderDataPacket(boolean replaceBlocks, byte roadWidth, RoadType roadType) {
        this.replaceBlocks = replaceBlocks;
        this.roadWidth = roadWidth;
        this.roadType = roadType;
    }

    @Override
    public void encode(RoadBuilderDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeByte(packet.roadWidth);
        buffer.writeEnum(packet.roadType);
    }

    @Override
    public RoadBuilderDataPacket decode(FriendlyByteBuf buffer) {
        boolean replaceBlocks = buffer.readBoolean();
        byte roadWidth = buffer.readByte();
        RoadType roadType = buffer.readEnum(RoadType.class);
        return new RoadBuilderDataPacket(replaceBlocks, roadWidth, roadType);
    }

    @Override
    public void handle(RoadBuilderDataPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
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
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
