package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PaintBrushPacket extends NetworkPacketData {

    private static final String NBT_DATA = "Data";

    private int pattern;

    public PaintBrushPacket(DLStatus status) {
        super(status);
    }

    public PaintBrushPacket(int pattern) {
        super(DLStatus.OK);
        this.pattern = pattern;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putInt(NBT_DATA, pattern);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pattern = nbt.getInt(NBT_DATA);
    }
    
    public static void handle(PaintBrushPacket packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();

        if(sender.getMainHandItem().getItem() instanceof BrushItem) {
            CompoundTag nbt = sender.getMainHandItem().getTag();
            nbt.putInt(BrushItem.NBT_PATTERN, packet.pattern);
        } else if (sender.getOffhandItem().getItem() instanceof BrushItem) {
            
            CompoundTag nbt = sender.getOffhandItem().getTag();
            nbt.putInt(BrushItem.NBT_PATTERN, packet.pattern);
        }
        sender.getInventory().setChanged();
    }
}
