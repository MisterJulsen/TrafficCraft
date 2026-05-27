package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.data.textures.TextureIdentifier;
import de.mrjulsen.trafficcraft.data.textures.TextureRepository;
import de.mrjulsen.trafficcraft.data.textures.data.NbtTextureData;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class GetTexturePacket {

    private static final String NBT_DATA = "Data";

    public static class Request extends NetworkPacketData {

        private TextureIdentifier key;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(TextureIdentifier key) {
            super(DLStatus.OK);
            this.key = key;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.put(NBT_DATA, key.toNbt());
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.key = TextureIdentifier.fromNbt(nbt.getCompound(NBT_DATA));
        }
    }

    public static class Response extends NetworkPacketData {

        private Optional<NbtTextureData> data;

        public Response(DLStatus status) {
            super(status);
        }

        public Response(Optional<NbtTextureData> data) {
            super(DLStatus.OK);
            this.data = data;
        }

        @Override
        protected void write(CompoundTag nbt) {
            this.data.ifPresent(d -> nbt.put(NBT_DATA, d.toNbt()));
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.data = Optional.ofNullable(nbt.contains(NBT_DATA) ? NbtTextureData.fromNbt(nbt.getCompound(NBT_DATA)) : null);
        }

        public Optional<NbtTextureData> getTextureData() {
            return data;
        }
    }

    public static Response handle(Request packet, NetworkPacketContext context) {
        return new Response(TextureRepository.loadCustom(packet.key));
    }

}
