package de.mrjulsen.trafficcraft.client;

import java.util.Arrays;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    private static final String TEXTURE_PATH = "block/traffic_light";
    
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            return;
        }
        
        Arrays.stream(TrafficLightIcon.values())
            .forEach(
                x -> Arrays.stream(TrafficLightColor.values())
                    .filter(y -> x.isApplicableToColor(y))
                    .forEach(y -> {
                        ResourceLocation loc = null;
                        if (x == TrafficLightIcon.NONE && y == TrafficLightColor.NONE) {
                            loc = new ResourceLocation(ModMain.MOD_ID, String.format("%s/off", TEXTURE_PATH));
                        } else {
                            loc = new ResourceLocation(ModMain.MOD_ID, String.format("%s/%s_%s", TEXTURE_PATH, x.getName(), y.getName()));
                        }
                        event.addSprite(loc);
                    }));
    }
}
