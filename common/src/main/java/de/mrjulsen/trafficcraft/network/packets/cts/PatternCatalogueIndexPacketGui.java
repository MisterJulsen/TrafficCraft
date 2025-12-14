package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PatternCatalogueIndexPacketGui {

    public static class Request extends NetworkPacketData {
        private static final String NBT_DATA = "Data";

        private int index;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(int index) {
            super(DLStatus.OK);
            this.index = index;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putInt(NBT_DATA, index);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.index = nbt.getInt(NBT_DATA);
        }
    }

    public static class Response extends NetworkPacketData {

        public Response(DLStatus status) {
            super(status);
        }

        public Response() {
            super(DLStatus.OK);
        }

        @Override
        protected void write(CompoundTag nbt) {
        }

        @Override
        protected void read(CompoundTag nbt) {
        }
    }

    public static Response handle(Request packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
            final ItemStack stack = menu.patternSlot.getItem();
            if (!(stack.getItem() instanceof PatternCatalogueItem))
                return new Response();

            PatternCatalogueItem.setSelectedIndex(stack, packet.index);
            menu.patternSlot.set(stack);
            menu.patternSlot.setChanged();
            menu.broadcastChanges();
            return new Response();
        }
        return new Response();
    }
}
