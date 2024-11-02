package de.mrjulsen.trafficcraft.client.screen.menu;

import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<TrafficSignWorkbenchMenu>> TRAFFIC_SIGN_WORKBENCH_MENU = MENUS.register("traffic_sign_workbench_menu", () -> new MenuType<>(TrafficSignWorkbenchMenu::new, FeatureFlags.DEFAULT_FLAGS));

    //public static final RegistrySupplier<MenuType<TrafficSignWorkbenchMenu>> TRAFFIC_SIGN_WORKBENCH_MENU = registerMenuType(TrafficSignWorkbenchMenu::new, "traffic_sign_workbench_menu");
//
//
    //private static <T extends AbstractContainerMenu>RegistrySupplier<MenuType<T>> registerMenuType(MenuSupplier<T> factory, String name) {
    //    return MENUS.register(name, () -> new MenuType<T>(factory, FeatureFlags.DEFAULT_FLAGS));
    //}

    public static void init() {
        MENUS.register();
    }

}