package de.mrjulsen.trafficcraft.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.trafficcraft.data.IAgeable.AgingType;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;

public final class AgingManager {
    private static final Map<IAgeable, MutableSingle<Integer>> ageableObjects = new HashMap<>();

    public static void add(IAgeable obj) {
        ageableObjects.computeIfAbsent(obj, x -> new MutableSingle<>(0)).setFirst(0);
    }

    public static void remove(IAgeable obj) {        
        ageableObjects.remove(obj);
    }

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(mc -> {
            Map<IAgeable, MutableSingle<Integer>> map = new HashMap<>(ageableObjects);
            Iterator<Entry<IAgeable, MutableSingle<Integer>>> iterator = map.entrySet().iterator();
            iterator.forEachRemaining(x -> {
                if (x.getKey().getAgingType() != AgingType.TICK) {
                    return;
                }
                int age = x.getValue().getFirst() + 1;
                x.getValue().setFirst(age);
                x.getKey().onAging(age);
            });
        });
        
        ClientGuiEvent.RENDER_POST.register((screen, poseStack, mouseX, mouseY, partialTick) -> {
            Map<IAgeable, MutableSingle<Integer>> map = new HashMap<>(ageableObjects);
            Iterator<Entry<IAgeable, MutableSingle<Integer>>> iterator = map.entrySet().iterator();
            iterator.forEachRemaining(x -> {
                if (x.getKey().getAgingType() != AgingType.RENDER) {
                    return;
                }
                int age = x.getValue().getFirst() + 1;
                x.getValue().setFirst(age);
                x.getKey().onAging(age);
            });
        });
    }
}
