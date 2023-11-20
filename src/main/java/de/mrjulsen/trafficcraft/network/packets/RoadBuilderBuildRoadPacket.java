package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.data.Location;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class RoadBuilderBuildRoadPacket {

    private Location pos1;
    private Location pos2;
    private byte roadWidth;
    private boolean replaceBlocks;
    private RoadType roadType;
    
    public RoadBuilderBuildRoadPacket(Location pos1, Location pos2, byte roadWidth, boolean replaceBlocks, RoadType roadType) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.roadWidth = roadWidth;
        this.replaceBlocks = replaceBlocks;
        this.roadType = roadType;
    }

    public static void encode(RoadBuilderBuildRoadPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.pos1.toNbt());
        buffer.writeNbt(packet.pos2.toNbt());
        buffer.writeByte(packet.roadWidth);
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeEnum(packet.roadType);
    }

    public static RoadBuilderBuildRoadPacket decode(FriendlyByteBuf buffer) {
        Location pos1 = Location.fromNbt(buffer.readNbt());
        Location pos2 = Location.fromNbt(buffer.readNbt());
        byte roadWidth = buffer.readByte();
        boolean replaceBlocks = buffer.readBoolean();
        RoadType roadType = buffer.readEnum(RoadType.class);

        return new RoadBuilderBuildRoadPacket(pos1, pos2, roadWidth, replaceBlocks, roadType);
    }

    public static void handle(RoadBuilderBuildRoadPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            Level level = sender.getLevel();
            ItemStack item = null;
            InteractionHand hand = null;

            if (sender.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RoadConstructionTool) {
                item = sender.getItemInHand(InteractionHand.MAIN_HAND);
                hand = InteractionHand.MAIN_HAND;
            } else if (sender.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof RoadConstructionTool) {
                item = sender.getItemInHand(InteractionHand.OFF_HAND);
                hand = InteractionHand.OFF_HAND;
            } else {
                return;
            }

            RoadConstructionTool.buildRoad(
                level,
                sender,
                hand,
                item,
                packet.pos1.getLocationAsVec3(),
                packet.pos2.getLocationAsVec3(), 
                packet.roadWidth,
                packet.replaceBlocks,
                packet.roadType
            );
        });
        context.get().setPacketHandled(true);
    }
}
