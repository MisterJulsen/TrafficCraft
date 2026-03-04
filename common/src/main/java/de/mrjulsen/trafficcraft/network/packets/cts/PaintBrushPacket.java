package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.components.BrushComponent;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();

            if (sender.getMainHandItem().getItem() instanceof BrushItem brush) {
                ItemStack stack = sender.getMainHandItem();
                BrushComponent comp = brush.getComponent(stack);
                brush.setComponent(stack, new BrushComponent(packet.pattern, comp.paintAmount(), comp.colorId()));
            } else if (sender.getOffhandItem().getItem() instanceof BrushItem brush) {
                ItemStack stack = sender.getOffhandItem();
                BrushComponent comp = brush.getComponent(stack);
                brush.setComponent(stack, new BrushComponent(packet.pattern, comp.paintAmount(), comp.colorId()));
            }
            sender.getInventory().setChanged();
        });
    }
}
