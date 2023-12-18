package de.mrjulsen.trafficcraft.block.data;

public interface IIteratableEnum<T extends Enum<T>> {
    T[] getValues();

    @SuppressWarnings("unchecked")
    default T next() {
        return getValues()[(((T)this).ordinal() + 1) % getValues().length];
    }
}
