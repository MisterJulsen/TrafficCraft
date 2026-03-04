package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {

    public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, DLUtils.resourceLocation("c:tools/wrench"));

    public static void init() {
    }
}
