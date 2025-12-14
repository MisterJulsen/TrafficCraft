package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem.LinkerMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class LinkerModePacket extends NetworkPacketData {

    private static final String NBT_DATA = "Data";

    private LinkerMode mode;

    public LinkerModePacket(DLStatus status) {
        super(status);
    }

    public LinkerModePacket(LinkerMode mode) {
        super(DLStatus.OK);
        this.mode = mode;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putInt(NBT_DATA, mode.getIndex());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.mode = LinkerMode.getByIndex(nbt.getInt(NBT_DATA));
    }
    
    public static void handle(LinkerModePacket packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.getMainHandItem().getItem() instanceof TrafficLightLinkerItem) {
            TrafficLightLinkerItem.setMode(sender.getMainHandItem(), packet.mode);
        } else if (sender.getOffhandItem().getItem() instanceof TrafficLightLinkerItem) { 
            TrafficLightLinkerItem.setMode(sender.getOffhandItem(), packet.mode);
        }
        sender.getInventory().setChanged();
    }
}
