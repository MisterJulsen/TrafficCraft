package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TrafficSignPatternPacket {

    public static class Request extends NetworkPacketData {
        private static final String NBT_NAME = "Name";
        private static final String NBT_INDEX = "Index";
        
        private NamedTrafficSignTextureReference reference;
        private int index;

        public Request(DLStatus status) {
            super(status);
        }

        /**
         * @param reference TrafficSign data.
         * @param index The index of the slot you want to replace or -1 to create a new pattern.
         */
        public Request(NamedTrafficSignTextureReference reference, int index) {
            super(DLStatus.OK);
            this.index = index;
            this.reference = reference;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putInt(NBT_INDEX, index);
            nbt.put(NBT_NAME, reference.toNbt());
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.index = nbt.getInt(NBT_INDEX);
            this.reference = NamedTrafficSignTextureReference.fromNbt(nbt.getCompound(NBT_NAME));
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

            if (packet.index >= 0) {
                PatternCatalogueItem.replacePattern(stack, packet.reference, packet.index);
            } else {
                PatternCatalogueItem.setPattern(stack, packet.reference);
            }
            menu.patternSlot.set(stack);
            menu.patternSlot.setChanged();
            menu.broadcastChanges();

            DLUtils.giveAdvancement(sender, TrafficCraft.MOD_ID, "create_traffic_sign_pattern", "requirement");
        }
        return new Response();
    }
}
