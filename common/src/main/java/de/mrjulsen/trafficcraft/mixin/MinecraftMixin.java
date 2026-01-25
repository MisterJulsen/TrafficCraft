package de.mrjulsen.trafficcraft.mixin;

import de.mrjulsen.trafficcraft.client.ClientWrapper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "runTick", at = @At("TAIL"))
    public void tc$runAfterClientTick(boolean render, CallbackInfo ci) {
        ClientWrapper.runAllScheduledRenderTasks();
    }
}
