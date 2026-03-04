package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureManager;
import net.minecraft.nbt.CompoundTag;

public class GetTrafficSignTexturePacket {

    private static final String NBT_DATA = "Data";

    public static class Request extends NetworkPacketData {        

        private String name;

        public Request(DLStatus status) {
            super(status);
        }

        public Request(String name) {
            super(DLStatus.OK);
            this.name = name;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.putString(NBT_DATA, name);
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.name = nbt.getString(NBT_DATA);
        }
    }
    
    public static class Response extends NetworkPacketData {        

        private TrafficSignTextureData data;

        public Response(DLStatus status) {
            super(status);
        }

        public Response(TrafficSignTextureData data) {
            super(DLStatus.OK);
            this.data = data;
        }

        @Override
        protected void write(CompoundTag nbt) {
            nbt.put(NBT_DATA, data.serializeNbt());
        }

        @Override
        protected void read(CompoundTag nbt) {
            this.data = TrafficSignTextureData.deserializeNbt(nbt.getCompound(NBT_DATA));
        }

        public TrafficSignTextureData getData() {
            return data;
        }
    }

    public static Response handle(Request packet, NetworkPacketContext context) {
        return context.queueResult(() -> new Response(TrafficSignTextureManager.load(packet.name)));
    }
    
}
