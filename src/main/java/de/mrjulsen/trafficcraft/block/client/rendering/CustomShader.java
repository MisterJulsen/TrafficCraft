package de.mrjulsen.trafficcraft.block.client.rendering;

import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomShader {

    static final ShaderTracker GLOW = new ShaderTracker();

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        registerShader(event, new ResourceLocation(ModMain.MOD_ID, "rendertype_glow"), DefaultVertexFormat.BLOCK, GLOW);
    }

    private static void registerShader(RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), shaderLocation, vertexFormat), tracker::setInstance);
        ModMain.LOGGER.debug("Loaded shader: " + shaderLocation.toString());
    }

    static class ShaderTracker {

        private ShaderInstance instance;
        final ShaderStateShard shard = new ShaderStateShard(() -> this.instance);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
            ModMain.LOGGER.debug("Test 1");
        }
    }
}
