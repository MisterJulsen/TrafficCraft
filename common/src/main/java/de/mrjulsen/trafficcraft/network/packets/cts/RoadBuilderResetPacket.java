package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class RoadBuilderResetPacket extends NetworkPacketData {
    
    public RoadBuilderResetPacket(DLStatus status) {
        super(status);
    }

    public RoadBuilderResetPacket() {
        super(DLStatus.OK);
    }

    @Override
    protected void write(CompoundTag nbt) {
    }

    @Override
    protected void read(CompoundTag nbt) {
    }
    
    public static void handle(RoadBuilderResetPacket packet, NetworkPacketContext context) {        
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();
            if (sender.getMainHandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getMainHandItem());
            } else if (sender.getOffhandItem().getItem() instanceof RoadConstructionTool) {
                RoadConstructionTool.reset(sender.getOffhandItem());
            }
            sender.getInventory().setChanged();
        });
    }
}
