package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.components.TrafficLightLinkerComponent;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem;
import de.mrjulsen.trafficcraft.item.TrafficLightLinkerItem.LinkerMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();
            ItemStack stack;

            if ((stack = sender.getMainHandItem()).getItem() instanceof TrafficLightLinkerItem item) {
                TrafficLightLinkerComponent comp = item.getComponent(stack);
                item.setComponent(stack, new TrafficLightLinkerComponent(comp.location(), packet.mode, comp.targetBlockName()));
            } else if ((stack = sender.getOffhandItem()).getItem() instanceof TrafficLightLinkerItem item) {
                TrafficLightLinkerComponent comp = item.getComponent(stack);
                item.setComponent(stack, new TrafficLightLinkerComponent(comp.location(), packet.mode, comp.targetBlockName()));
            }

            sender.getInventory().setChanged();
        });
    }
}
