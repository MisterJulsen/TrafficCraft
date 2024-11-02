package de.mrjulsen.trafficcraft.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface IUseDataComponent<T> {

    DataComponentType<T> getComponentType();
    T emptyComponent();

    default boolean hasComponent(ItemStack stack) {
        return stack.has(getComponentType());
    }

    default T getComponent(ItemStack stack) {
        return stack.getOrDefault(getComponentType(), emptyComponent());
    }

    default T setComponent(ItemStack stack, T data) {
        return stack.set(getComponentType(), data);
    }
}
