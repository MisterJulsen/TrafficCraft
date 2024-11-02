package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ColorPaletteItemPacket extends BaseNetworkPacket<ColorPaletteItemPacket> {
    
    private int color;
    private byte index;

    public ColorPaletteItemPacket() {}

    public ColorPaletteItemPacket(int color, int index) {
        this.color = color;
        this.index = (byte)index;
    }

    @Override
    public void encode(ColorPaletteItemPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.color);
        buffer.writeByte(packet.index);
    }

    @Override
    public ColorPaletteItemPacket decode(RegistryFriendlyByteBuf buffer) {
        int color = buffer.readInt();
        byte index = buffer.readByte();

        return new ColorPaletteItemPacket(color, index);
    }
    
    @Override
    public void handle(ColorPaletteItemPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.colorSlot.getItem();
                if (!(stack.getItem() instanceof ColorPaletteItem))
                    return;

                ColorPaletteItem.setColor(stack, packet.index, packet.color);
                menu.colorSlot.set(stack);
                menu.colorSlot.setChanged();
                menu.broadcastChanges();
                
                DLUtils.giveAdvancement(sender, TrafficCraft.MOD_ID, "store_color_palette", "requirement");
            }
        });
    }
}
