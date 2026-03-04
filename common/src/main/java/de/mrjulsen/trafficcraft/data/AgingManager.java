package de.mrjulsen.trafficcraft.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.mrjulsen.mcdragonlib.util.Holder.MutableHolder;
import de.mrjulsen.trafficcraft.data.IAgeable.AgingType;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;

public final class AgingManager {
    private static final Map<IAgeable, MutableHolder<Integer>> ageableObjects = new HashMap<>();

    public static void add(IAgeable obj) {
        ageableObjects.computeIfAbsent(obj, x -> new MutableHolder<>(0)).set(0);
    }

    public static void remove(IAgeable obj) {        
        ageableObjects.remove(obj);
    }

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(mc -> {
            Map<IAgeable, MutableHolder<Integer>> map = new HashMap<>(ageableObjects);
            Iterator<Entry<IAgeable, MutableHolder<Integer>>> iterator = map.entrySet().iterator();
            iterator.forEachRemaining(x -> {
                if (x.getKey().getAgingType() != AgingType.TICK) {
                    return;
                }
                int age = x.getValue().get() + 1;
                x.getValue().set(age);
                x.getKey().onAging(age);
            });
        });
        
        ClientGuiEvent.RENDER_POST.register((screen, poseStack, mouseX, mouseY, partialTick) -> {
            Map<IAgeable, MutableHolder<Integer>> map = new HashMap<>(ageableObjects);
            Iterator<Entry<IAgeable, MutableHolder<Integer>>> iterator = map.entrySet().iterator();
            iterator.forEachRemaining(x -> {
                if (x.getKey().getAgingType() != AgingType.RENDER) {
                    return;
                }
                int age = x.getValue().get() + 1;
                x.getValue().set(age);
                x.getKey().onAging(age);
            });
        });
    }
}
