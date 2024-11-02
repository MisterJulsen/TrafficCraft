package de.mrjulsen.trafficcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import de.mrjulsen.trafficcraft.init.ClientInit;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    @Overwrite
    public static ClientTooltipComponent create(TooltipComponent visualTooltipComponent) {
        if (visualTooltipComponent instanceof BundleTooltip bundleTooltip) {
            return new ClientBundleTooltip(bundleTooltip.contents());
        } else if (visualTooltipComponent instanceof ClientActivePlayersTooltip.ActivePlayersTooltip activePlayersTooltip) {
            return new ClientActivePlayersTooltip(activePlayersTooltip);
        } else {            
            ClientTooltipComponent result = ClientInit.getClientTooltipComponent(visualTooltipComponent);
            if (result != null) {
                return result;
            } else {
                throw new IllegalArgumentException("Unknown TooltipComponent");
            }
        }
    }
}
