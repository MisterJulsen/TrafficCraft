package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import dev.architectury.platform.Platform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    public static final TagKey<Block> POST_EXTENSION = TagKey.create(Registries.BLOCK, DLUtils.resourceLocation(TrafficCraft.MOD_ID, "requires_post_extension"));

    public static void init() {
    }
}
