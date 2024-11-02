package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.components.RoadConstructionToolComponent;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RoadBuilderDataPacket extends BaseNetworkPacket<RoadBuilderDataPacket> {

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
    public void encode(RoadBuilderDataPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeByte(packet.roadWidth);
        buffer.writeEnum(packet.roadType);
    }

    @Override
    public RoadBuilderDataPacket decode(RegistryFriendlyByteBuf buffer) {
        boolean replaceBlocks = buffer.readBoolean();
        byte roadWidth = buffer.readByte();
        RoadType roadType = buffer.readEnum(RoadType.class);
        return new RoadBuilderDataPacket(replaceBlocks, roadWidth, roadType);
    }
    
    @Override
    public void handle(RoadBuilderDataPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
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
