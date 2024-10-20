package de.mrjulsen.trafficcraft.data;

import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import net.minecraft.resources.ResourceLocation;

public record TrafficSignTextureMetadata(ResourceLocation location, TrafficSignShape shape, int id, short width, short height) {}
