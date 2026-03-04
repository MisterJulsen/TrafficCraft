package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import net.minecraft.nbt.CompoundTag;

public class CreateNewTrafficSignTexturePacket {

    public static class Request extends NetworkPacketData {
        private static final String NBT_DATA = "Data";

        private TrafficSignTextureData data;    

        public Request(DLStatus status) {
            super(status);
        }

        public Request(TrafficSignTextureData data) {
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
        return context.queueResult(() -> {
            packet.data.save();
            return new Response();
        });
    }
    
}
