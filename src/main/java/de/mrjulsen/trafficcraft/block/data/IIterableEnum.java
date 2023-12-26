package de.mrjulsen.trafficcraft.block.data;

public interface IIterableEnum<T extends Enum<T>> {
    T[] getValues();

    @SuppressWarnings("unchecked")
    default T next() {
        return getValues()[(((T)this).ordinal() + 1) % getValues().length];
    }

    @SuppressWarnings("unchecked")
    default T previous() {
        return getValues()[((T)this).ordinal() > 0 ? ((T)this).ordinal() - 1 % getValues().length : getValues().length - 1 % getValues().length];
    }
}
