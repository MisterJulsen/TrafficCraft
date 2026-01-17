package de.mrjulsen.trafficcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.trafficcraft.client.ClientWrapper;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    
    @Inject(method = "flipFrame", remap = false, at = @At(value = "TAIL"))
    private static void trafficcraft$flipFrame(long l, CallbackInfo ci) {
        ClientWrapper.runAllScheduledRenderTasks();
    }
}
