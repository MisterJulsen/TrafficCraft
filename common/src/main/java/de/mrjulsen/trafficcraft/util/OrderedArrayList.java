package de.mrjulsen.trafficcraft.util;

import java.util.ArrayList;
import java.util.Collection;

public class OrderedArrayList<T> extends ArrayList<T> {

    public OrderedArrayList() {}

    public OrderedArrayList(Collection<T> initialElements) {
        addAll(initialElements);
    }

    public void moveForth(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex + amount, obj);
    }

    public void moveBack(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex - amount, obj);
    }

    public void moveToStart(int srcIndex) {
        moveBack(srcIndex, srcIndex);
    }

    public void moveToEnd(int srcIndex) {
        moveForth(srcIndex, size() - 1 - srcIndex);
    }
}

