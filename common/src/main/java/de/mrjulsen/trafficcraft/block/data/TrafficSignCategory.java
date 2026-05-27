package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public enum TrafficSignCategory {
    CIRCLE((byte)0, "circle"),
    SQUARE((byte)1, "square"),
    DIAMOND((byte)2, "diamond"),
    TRIANGLE((byte)3, "triangle"),
    SUPPLEMENTARY((byte)4, "supplementary"),
    MISC(Byte.MAX_VALUE, "misc");

    private final byte index;
    private final String name;

    private static final Object2ObjectMap<Byte, TrafficSignCategory> BY_INDEX = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<String, TrafficSignCategory> BY_NAME = new Object2ObjectOpenHashMap<>();

    static {
        for (TrafficSignCategory category : TrafficSignCategory.values()) {
            BY_INDEX.put(category.index, category);
            BY_NAME.put(category.name, category);
        }
    }

    TrafficSignCategory(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getIconLocation() {
        return DLUtils.resourceLocation(TrafficCraft.MOD_ID, String.format("textures/block/sign/icons/%s.png", getName()));
    }

    public static Optional<TrafficSignCategory> getByIndex(int index) {
        return Optional.ofNullable(BY_INDEX.get(index));
    }

    public static Optional<TrafficSignCategory> getByName(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }
}
