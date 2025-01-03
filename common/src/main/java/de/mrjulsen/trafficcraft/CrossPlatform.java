package de.mrjulsen.trafficcraft;

import java.util.function.Function;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public final class CrossPlatform {
        
    @ExpectPlatform
    public static void registerConfig() {
        throw new AssertionError();
    }

    @Environment(EnvType.CLIENT)
    @ExpectPlatform
    public static <T extends TooltipComponent> void registerTooltipComponentFactory(Class<T> cls, Function<? super T, ? extends ClientTooltipComponent> factory) {
        throw new AssertionError();
    }
}
