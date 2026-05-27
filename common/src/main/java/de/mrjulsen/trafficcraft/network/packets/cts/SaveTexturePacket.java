package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.data.NamedTextureKey;
import de.mrjulsen.trafficcraft.data.textures.TextureRepository;
import de.mrjulsen.trafficcraft.data.textures.data.NbtTextureData;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SaveTexturePacket {

    private static final String NBT_KEY = "Key";
    private static final String NBT_PAYLOAD = "Payload";
    private static final String NBT_INDEX = "Index";

    public static class Request extends NetworkPacketData {

        private NamedTextureKey key;
        private NbtTextureData payload;
        private int index = -1;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(NamedTextureKey key, NbtTextureData payload, int index) {
            super(DLStatus.OK);
            this.key = key;
            this.payload = payload;
            this.index = index;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.put(NBT_KEY, key.toNbt());
            nbt.put(NBT_PAYLOAD, payload.toNbt());
            nbt.putInt(NBT_INDEX, index);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.key = NamedTextureKey.fromNbt(nbt.getCompound(NBT_KEY));
            this.payload = NbtTextureData.fromNbt(nbt.getCompound(NBT_PAYLOAD));
            this.index = nbt.getInt(NBT_INDEX);
        }
    }

    public static class Response extends NetworkPacketData {
        public Response(DLStatus status) {
            super(status);
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
                return new Response(DLStatus.CANCEL);

            if (packet.index >= 0) {
                PatternCatalogueItem.replacePattern(stack, packet.key, packet.index);
            } else {
                PatternCatalogueItem.setPattern(stack, packet.key);
            }
            menu.patternSlot.set(stack);
            menu.patternSlot.setChanged();
            menu.broadcastChanges();

            DLUtils.giveAdvancement(sender, TrafficCraft.MOD_ID, "create_traffic_sign_pattern", "requirement");
        }

        TextureRepository.saveCustom(packet.key.textureKey(), packet.payload);
        return new Response(DLStatus.OK);
    }
}
