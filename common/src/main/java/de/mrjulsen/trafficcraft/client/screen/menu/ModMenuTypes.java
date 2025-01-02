package de.mrjulsen.trafficcraft.client.screen.menu;

import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(TrafficCraft.MOD_ID, Registry.MENU_REGISTRY);

    public static final RegistrySupplier<MenuType<TrafficSignWorkbenchMenu>> TRAFFIC_SIGN_WORKBENCH_MENU = registerMenuType(TrafficSignWorkbenchMenu::new, "traffic_sign_workbench_menu");


    private static <T extends AbstractContainerMenu>RegistrySupplier<MenuType<T>> registerMenuType(MenuSupplier<T> factory, String name) {
        return MENUS.register(name, () -> new MenuType<T>(factory));
    }

    public static void register() {
        MENUS.register();
    }

}