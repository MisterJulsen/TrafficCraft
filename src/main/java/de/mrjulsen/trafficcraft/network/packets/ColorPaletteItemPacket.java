package de.mrjulsen.trafficcraft.network.packets;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class ColorPaletteItemPacket
{
    private int color;
    private byte index;

    public ColorPaletteItemPacket(int color, int index) {
        this.color = color;
        this.index = (byte)index;
    }

    public static void encode(ColorPaletteItemPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.color);
        buffer.writeByte(packet.index);
    }

    public static ColorPaletteItemPacket decode(FriendlyByteBuf buffer) {
        int color = buffer.readInt();
        byte index = buffer.readByte();

        return new ColorPaletteItemPacket(color, index);
    }

    public static void handle(ColorPaletteItemPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.colorSlot.getItem();
                if (!(stack.getItem() instanceof ColorPaletteItem))
                    return;

                ColorPaletteItem.setColor(stack, packet.index, packet.color);
                menu.colorSlot.set(stack);
                menu.colorSlot.setChanged();
                menu.broadcastChanges();
                
                Utils.giveAdvancement(sender, "store_color_palette", "requirement");
            }
        });
        context.get().setPacketHandled(true);
    }
}
