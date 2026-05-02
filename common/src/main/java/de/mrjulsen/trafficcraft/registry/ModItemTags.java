package de.mrjulsen.trafficcraft.registry;

import dev.architectury.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, Platform.isForge() ? new ResourceLocation("forge:tools/wrench") : new ResourceLocation("c:wrenches"));

    public static void init() {
    }
}
