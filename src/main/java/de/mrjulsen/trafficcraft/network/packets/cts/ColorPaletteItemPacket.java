package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ColorPaletteItemPacket implements IPacketBase<ColorPaletteItemPacket> {
    
    private int color;
    private byte index;

    public ColorPaletteItemPacket() {}

    public ColorPaletteItemPacket(int color, int index) {
        this.color = color;
        this.index = (byte)index;
    }

    @Override
    public void encode(ColorPaletteItemPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.color);
        buffer.writeByte(packet.index);
    }

    @Override
    public ColorPaletteItemPacket decode(FriendlyByteBuf buffer) {
        int color = buffer.readInt();
        byte index = buffer.readByte();

        return new ColorPaletteItemPacket(color, index);
    }

    @Override
    public void handle(ColorPaletteItemPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.colorSlot.getItem();
                if (!(stack.getItem() instanceof ColorPaletteItem))
                    return;

                ColorPaletteItem.setColor(stack, packet.index, packet.color);
                menu.colorSlot.set(stack);
                menu.colorSlot.setChanged();
                menu.broadcastChanges();
                
                Utils.giveAdvancement(sender, ModMain.MOD_ID, "store_color_palette", "requirement");
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
