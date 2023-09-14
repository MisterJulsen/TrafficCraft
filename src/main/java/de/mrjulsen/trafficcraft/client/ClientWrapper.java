package de.mrjulsen.trafficcraft.client;

import java.util.function.Supplier;

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
}
