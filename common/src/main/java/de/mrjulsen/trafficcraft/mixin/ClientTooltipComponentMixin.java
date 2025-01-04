package de.mrjulsen.trafficcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import de.mrjulsen.trafficcraft.init.ClientInit;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

    @PlatformOnly(value = PlatformOnly.FABRIC)
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At(value = "HEAD"), cancellable = true)
    private static void trafficcraft$create(TooltipComponent visualTooltipComponent, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        ClientTooltipComponent result = ClientInit.getClientTooltipComponent(visualTooltipComponent);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
