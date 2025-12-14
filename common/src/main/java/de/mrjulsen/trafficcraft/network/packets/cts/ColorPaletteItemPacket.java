package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.ColorPaletteItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ColorPaletteItemPacket {

    public static class Request extends NetworkPacketData {
        private static final String NBT_COLOR = "Color";
        private static final String NBT_INDEX = "Index";
        
        private int color;
        private byte index;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(int color, int index) {
            super(DLStatus.OK);
            this.color = color;
            this.index = (byte)index;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putInt(NBT_COLOR, color);
            nbt.putByte(NBT_INDEX, index);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.color = nbt.getInt(NBT_COLOR);
            this.index = nbt.getByte(NBT_INDEX);
        }
    }

    public static class Response extends NetworkPacketData {

        private static final String NBT_COLOR = "Color";
        
        private int color;

        public Response(DLStatus status) {
            super(status);
        }

        public Response(int color) {
            super(DLStatus.OK);
            this.color = color;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putInt(NBT_COLOR, color);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.color = nbt.getInt(NBT_COLOR);
        }

        public int getColor() {
            return color;
        }
    }
    
    public static Response handle(Request packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
            final ItemStack stack = menu.colorSlot.getItem();
            if (!(stack.getItem() instanceof ColorPaletteItem))
                return new Response(0);

            ColorPaletteItem.setColor(stack, packet.index, packet.color);
            menu.colorSlot.set(stack);
            menu.colorSlot.setChanged();
            menu.broadcastChanges();
            
            DLUtils.giveAdvancement(sender, TrafficCraft.MOD_ID, "store_color_palette", "requirement");
            return new Response(packet.color);
        }
        return new Response(0);
    }
}
