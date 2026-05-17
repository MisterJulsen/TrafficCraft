package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureManager;
import de.mrjulsen.trafficcraft.data.texture.ITexturePayload;
import de.mrjulsen.trafficcraft.data.texture.TextureKey;
import de.mrjulsen.trafficcraft.data.texture.TexturePayload;
import de.mrjulsen.trafficcraft.data.texture.TextureRepository;
import net.minecraft.nbt.CompoundTag;

public class GetTexturePacket {

    private static final String NBT_DATA = "Data";

    public static class Request extends NetworkPacketData {        

        private TextureKey key;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(TextureKey key) {
            super(DLStatus.OK);
            this.key = key;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.put(NBT_DATA, key.toNbt());
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.key = TextureKey.fromNbt(nbt.getCompound(NBT_DATA));
        }
    }
    
    public static class Response extends NetworkPacketData {        

        private ITexturePayload data;

        public Response(DLStatus status) {
            super(status);
        }

        public Response(ITexturePayload data) {
            super(DLStatus.OK);
            this.data = data;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.put(NBT_DATA, data.toNbt());
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.data = TexturePayload.fromNbt(nbt.getCompound(NBT_DATA));
        }

        public ITexturePayload getPayload() {
            return data;
        }
    }

    public static Response handle(Request packet, NetworkPacketContext context) {
        return new Response(TextureRepository.load(packet.key));
    }
    
}
