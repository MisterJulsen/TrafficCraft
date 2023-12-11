package de.mrjulsen.trafficcraft.client;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.common.IIdentifiable;
import de.mrjulsen.mcdragonlib.utils.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.PaintBrushScreen;
import de.mrjulsen.trafficcraft.client.screen.RoadConstructionToolScreen;
import de.mrjulsen.trafficcraft.client.screen.StreetLampScheduleScreen;
import de.mrjulsen.trafficcraft.client.screen.TownSignScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightConfigScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightControllerScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignPatternSelectionScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignWorkbenchUpdateClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class ClientWrapper {

    public static void showPaintBrushScreen(int pattern, int paint, int color, float scroll) {        
        Minecraft.getInstance().setScreen(new PaintBrushScreen(pattern, paint, color, scroll));
    }

    public static void showSignPatternSelectionScreen(ItemStack stack) {        
        Minecraft.getInstance().setScreen(new TrafficSignPatternSelectionScreen(stack));
    }

    public static void showStreetLampScheduleScreen(int turnOnTime, int turnOfftime, TimeFormat format) {        
        Minecraft.getInstance().setScreen(new StreetLampScheduleScreen(turnOnTime, turnOfftime, format));
    }

    public static void showTrafficLightConfigScreen(BlockPos pos, Level level) {
        Minecraft.getInstance().setScreen(new TrafficLightConfigScreen(pos, level));
    }

    public static void showTrafficLightControllerScreen(BlockPos pos, Level level) {
        Minecraft.getInstance().setScreen(new TrafficLightControllerScreen(pos, level));
    }

        public static void showWritableSignScreen(WritableTrafficSignBlockEntity pSign) {
        Minecraft.getInstance().setScreen(new WritableSignScreen(pSign));
    }

    public static void showTownSignScreen(TownSignBlockEntity pSign, TownSignBlock.ETownSignSide side) {
        Minecraft.getInstance().setScreen(new TownSignScreen(pSign, side));
    }

    
    @SuppressWarnings("resource")
    public static void handleTrafficSignWorkbenchUpdateClientPacket(TrafficSignWorkbenchUpdateClientPacket packet, Supplier<NetworkEvent.Context> ctx) { 
        if (Minecraft.getInstance().screen instanceof TrafficSignWorkbenchGui screen) {
            screen.updatePreview();
        }
    }

    public synchronized static <B extends IIdentifiable> void clearTexture(B id) {
        try {
            TrafficSignTextureCacheClient.clear(id);
        } catch (Exception e) {
            ModMain.LOGGER.warn("Unable to clear texture.", e);
        }
    }

    public static void handleTrafficSignTextureResetPacket(TrafficSignTextureResetPacket packet, Supplier<NetworkEvent.Context> ctx) { 
        TrafficSignTextureCacheClient.clear(packet.id);
    }

    public static void showRoadConstructionToolScreen(ItemStack itemstack, int blocksCount, int slopesCount) {
        Minecraft.getInstance().setScreen(new RoadConstructionToolScreen(itemstack, blocksCount, slopesCount));
    }
}
