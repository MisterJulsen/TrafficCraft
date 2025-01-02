package de.mrjulsen.trafficcraft.client;

import de.mrjulsen.mcdragonlib.block.WritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen;
import de.mrjulsen.mcdragonlib.core.IIdentifiable;
import de.mrjulsen.mcdragonlib.util.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightConfigScreen;
import de.mrjulsen.trafficcraft.client.screen.PaintBrushScreen;
import de.mrjulsen.trafficcraft.client.screen.RoadConstructionToolScreen;
import de.mrjulsen.trafficcraft.client.screen.StreetLampScheduleScreen;
import de.mrjulsen.trafficcraft.client.screen.TownSignScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightControllerScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignPatternSelectionScreen;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.init.ClientInit;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientWrapper {

    public static void showPaintBrushScreen(int pattern, int paint, PaintColor color) {
        Minecraft.getInstance().setScreen(new PaintBrushScreen(pattern, paint, color));
    }

    public static void showSignPatternSelectionScreen(ItemStack stack) {        
        Minecraft.getInstance().setScreen(new TrafficSignPatternSelectionScreen(stack));
    }

    public static void showStreetLampScheduleScreen(int turnOnTime, int turnOfftime, TimeFormat format) {        
        Minecraft.getInstance().setScreen(new StreetLampScheduleScreen(turnOnTime, turnOfftime, format));
    }

    public static void showTrafficLightConfigScreen(Level level, BlockPos pos) {
        Minecraft.getInstance().setScreen(new TrafficLightConfigScreen(level, pos));
    }

    public static void showTrafficLightControllerScreen(BlockPos pos, Level level) {
        Minecraft.getInstance().setScreen(new TrafficLightControllerScreen(pos, level));
    }

        public static void showWritableSignScreen(WritableSignBlockEntity pSign) {
        Minecraft.getInstance().setScreen(new WritableSignScreen(pSign));
    }

    public static void showTownSignScreen(TownSignBlockEntity pSign, TownSignBlock.ETownSignSide side) {
        Minecraft.getInstance().setScreen(new TownSignScreen(pSign, side));
    }

    
    @SuppressWarnings("resource")
    public static void handleTrafficSignWorkbenchUpdateClientPacket(TrafficSignWorkbenchUpdateClientPacket packet) { 
        if (Minecraft.getInstance().screen instanceof TrafficSignWorkbenchGui screen) {
            screen.updatePreview();
        }
    }

    public synchronized static <B extends IIdentifiable> void clearTexture(B id) {
        try {
            TrafficSignTextureCacheClient.clear(id);
        } catch (Exception e) {
            TrafficCraft.LOGGER.warn("Unable to clear texture.", e);
        }
    }

    public static void showRoadConstructionToolScreen(ItemStack itemstack, int blocksCount, int slopesCount) {
        Minecraft.getInstance().setScreen(new RoadConstructionToolScreen(itemstack, blocksCount, slopesCount));
    }

    
	public static DynamicTexture getShapeTexture(int index) {
		return ClientInit.SHAPE_TEXTURES[index];
	}

    public static int getShapeTextureId(int index) {
        return getShapeTexture(index).getId();
    }
}
