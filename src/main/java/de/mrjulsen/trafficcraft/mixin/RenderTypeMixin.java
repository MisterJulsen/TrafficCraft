package de.mrjulsen.trafficcraft.mixin;

import de.mrjulsen.trafficcraft.client.RenderTypes;
import de.mrjulsen.trafficcraft.config.ERenderType;
import de.mrjulsen.trafficcraft.config.ModClientConfig;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class RenderTypeMixin {
    
    /*
    @Overwrite
    public static List<RenderType> chunkBufferLayers() {
        // Ersetzen Sie dies durch Ihre eigene Liste von RenderType-Elementen
        return ImmutableList.of(RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent(), RenderType.tripwire(), RenderTypes.GLOW_SOLID);
    }
    
    */

    
    @Inject(method = "chunkBufferLayers", at = @At("RETURN"), cancellable = true)
    private static void addCustomLayer(CallbackInfoReturnable<List<RenderType>> cir) {
        List<RenderType> layers = new ArrayList<>();
        layers.addAll(cir.getReturnValue());
        if (ModClientConfig.GLOW_RENDER_EFFECT.get() == ERenderType.DEFAULT) {
            layers.add(RenderTypes.GLOW_SOLID);
        }
        cir.setReturnValue(layers);
    }
    
}

