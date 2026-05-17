package de.mrjulsen.trafficcraft;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.trafficcraft.data.SafeDynamicTexture;
import de.mrjulsen.trafficcraft.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.data.AgingManager;
import de.mrjulsen.trafficcraft.init.ClientInitWrapper;
import de.mrjulsen.trafficcraft.init.ServerInit;
import de.mrjulsen.trafficcraft.world.ModWorldGen;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public final class TrafficCraft {
    public static final String MOD_ID = "trafficcraft";
    public static final String MOD_NAME = "TrafficCraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ServerInit.init();
        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientInitWrapper.init();
            AgingManager.init();
        }

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenuTypes.register();
        ModNetworkManager.init();
        ModCreativeModeTab.init();
        ModWorldGen.init();
        ModItemTags.init();
        ModBlockTags.init();
        ModRegistries.init();
        Regi.init();
            
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");
        CrossPlatform.registerConfig();
    }



    public static final SafeDynamicTexture EMPTY_TEXTURE;
    public static final ResourceLocation EMPTY_LOCATION;

    static {
        NativeImage img = new NativeImage(1, 1, false);
        img.setPixelRGBA(0, 0, 0x00000000);
        EMPTY_TEXTURE = new SafeDynamicTexture(img, true);
        EMPTY_LOCATION = new ResourceLocation(TrafficCraft.MOD_ID, "empty_sign");
        Minecraft.getInstance().getTextureManager().register(EMPTY_LOCATION, EMPTY_TEXTURE);
    }
}
