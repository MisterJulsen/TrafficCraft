package de.mrjulsen.trafficcraft;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public final class CrossPlatform {    
    
    @ExpectPlatform
    public static void setRenderLayer(Block block, RenderType type) {
        throw new AssertionError();
    }
    
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }
}
