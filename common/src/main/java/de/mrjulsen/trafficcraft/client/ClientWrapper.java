package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.mcdragonlib.block.DLWritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.gui.builtin.WritableSignScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightconfigScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignPatternSelectionScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.client.screen.PaintBrushScreen;
import de.mrjulsen.trafficcraft.client.screen.RoadConstructionToolScreen;
import de.mrjulsen.trafficcraft.client.screen.StreetLampScheduleScreen;
import de.mrjulsen.trafficcraft.client.screen.TownSignScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightControllerScreen;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.init.ClientInit;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import de.mrjulsen.trafficcraft.util.ETimeFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientWrapper {

    public static void showPaintBrushScreen(int pattern, int paint, PaintColor color) {
        DLWindow.openWindow(mgr -> new PaintBrushScreen(mgr, pattern, paint, color));
    }

    public static void showSignPatternSelectionScreen(ItemStack stack) {        
        DLWindow.openWindow(mgr -> new TrafficSignPatternSelectionScreen(mgr, stack));
    }

    public static void showStreetLampScheduleScreen(int turnOnTime, int turnOfftime, ETimeFormat format) {        
        DLWindow.openWindow(mgr -> new StreetLampScheduleScreen(mgr, turnOnTime, turnOfftime, format));
    }

    public static void showTrafficLightConfigScreen(Level level, BlockPos pos) {
        DLWindow.openWindow(mgr -> new TrafficLightconfigScreen(mgr, level, pos));
    }

    public static void showTrafficLightControllerScreen(BlockPos pos, Level level) {
        DLWindow.openWindow(mgr -> new TrafficLightControllerScreen(mgr, pos, level));
    }

    public static void showWritableSignScreen(DLWritableSignBlockEntity pSign) {
        Minecraft.getInstance().setScreen(new WritableSignScreen(pSign));
    }

    public static void showTownSignScreen(TownSignBlockEntity pSign, TownSignBlock.ETownSignSide side) {
        Minecraft.getInstance().setScreen(new TownSignScreen(pSign, side));
    }

    
    @SuppressWarnings("resource")
    public static void handleTrafficSignWorkbenchUpdateClientPacket(TrafficSignWorkbenchUpdateClientPacket packet) {
        if (Minecraft.getInstance().screen instanceof TrafficSignWorkbenchGui screen) {
            //screen.updatePreview();
        }
    }

    public static void showRoadConstructionToolScreen(ItemStack itemstack, int blocksCount, int slopesCount) {
        DLWindow.openWindow(mgr -> new RoadConstructionToolScreen(mgr, itemstack, blocksCount, slopesCount));
    }

    
	public static DynamicTexture getShapeTexture(int index) {
		return ClientInit.SHAPE_TEXTURES[index];
	}

    public static int getShapeTextureId(int index) {
        return getShapeTexture(index).getId();
    }
}
