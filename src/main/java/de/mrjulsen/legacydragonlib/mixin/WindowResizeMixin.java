package de.mrjulsen.legacydragonlib.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Window;

import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class WindowResizeMixin {
    @Shadow
    @Final
    private Window window;

    @Inject(method = "resizeDisplay()V", at = @At("TAIL"))
    private void updateWindowSize(CallbackInfo ci) {
        GuiUtils.updateWindowSize(window);
    }
}
