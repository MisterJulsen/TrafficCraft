package de.mrjulsen.trafficcraft;

import com.mojang.blaze3d.platform.NativeImage;
import de.mrjulsen.mcdragonlib.util.time.datapack.TimeSystemDatapackLoader;
import de.mrjulsen.trafficcraft.data.SafeDynamicTexture;
import de.mrjulsen.trafficcraft.data.textures.TextureDataManager;
import de.mrjulsen.trafficcraft.data.textures.TextureDataTypes;
import de.mrjulsen.trafficcraft.registry.*;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientReloadShadersEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.inventory.InventoryMenu;
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
        TextureDataTypes.init();

        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, TextureDataManager.INSTANCE);
            
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC, MOD_ID + "-common.toml");
        CrossPlatform.registerConfig();

        ClientReloadShadersEvent.EVENT.register((provider, sink) -> {
            NativeImage img = new NativeImage(1, 1, false);
            img.setPixelRGBA(0, 0, 0x00000000);
            EMPTY_TEXTURE = new SafeDynamicTexture(img, true);
            EMPTY_LOCATION = new ResourceLocation(TrafficCraft.MOD_ID, "empty_sign");
            Minecraft.getInstance().getTextureManager().register(EMPTY_LOCATION, EMPTY_TEXTURE);
        });
    }



    public static SafeDynamicTexture EMPTY_TEXTURE;
    public static ResourceLocation EMPTY_LOCATION;

}
