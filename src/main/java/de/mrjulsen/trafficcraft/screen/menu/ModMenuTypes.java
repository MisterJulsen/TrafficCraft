package de.mrjulsen.trafficcraft.screen.menu;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ModMain.MOD_ID);

    public static final RegistryObject<MenuType<TrafficSignWorkbenchMenu>> TRAFFIC_SIGN_WORKBENCH_MENU = registerMenuType(TrafficSignWorkbenchMenu::new, "traffic_sign_workbench_menu");


    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(MenuSupplier<T> factory, String name) {
        return MENUS.register(name, () -> new MenuType<T>(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

}