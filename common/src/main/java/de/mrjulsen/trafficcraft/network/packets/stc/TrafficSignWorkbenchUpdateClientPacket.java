package de.mrjulsen.trafficcraft.network.packets.stc;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.nbt.CompoundTag;

public class TrafficSignWorkbenchUpdateClientPacket extends NetworkPacketData {

    public TrafficSignWorkbenchUpdateClientPacket(DLStatus status) {
        super(status);
    }
    
    public TrafficSignWorkbenchUpdateClientPacket() {
        super(DLStatus.OK);
    }

    @Override
    protected void write(CompoundTag nbt) {
    }

    @Override
    protected void read(CompoundTag nbt) {
    }
    
    public static void handle(TrafficSignWorkbenchUpdateClientPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            ClientWrapper.handleTrafficSignWorkbenchUpdateClientPacket(packet);
        });
    }
}
