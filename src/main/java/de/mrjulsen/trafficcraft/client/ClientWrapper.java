package de.mrjulsen.trafficcraft.client;

import java.util.function.Supplier;

import de.mrjulsen.trafficcraft.block.client.TrafficSignTextureCacheClient;
import de.mrjulsen.trafficcraft.block.entity.IIdentifiable;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignWorkbenchUpdateClientPacket;
import de.mrjulsen.trafficcraft.screen.TrafficSignWorkbenchGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

public class ClientWrapper {
    
    @SuppressWarnings("resource")
    public static void handleTrafficSignWorkbenchUpdateClientPacket(TrafficSignWorkbenchUpdateClientPacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (Minecraft.getInstance().screen instanceof TrafficSignWorkbenchGui screen) {
            screen.updatePreview();
        }
    }

    public synchronized static <B extends IIdentifiable> void clearTexture(B id) {
        TrafficSignTextureCacheClient.clear(id);
    }

    public static void handleTrafficSignTextureResetPacket(TrafficSignTextureResetPacket packet, Supplier<NetworkEvent.Context> ctx) { 
        TrafficSignTextureCacheClient.clear(packet.id);
    }
}
