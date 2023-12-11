package de.mrjulsen.trafficcraft.client.model;

import javax.annotation.Nullable;

import net.minecraft.client.resources.model.ModelState;

public record ModelKey(boolean generating, boolean collecting, boolean actuallyGenerating, @Nullable ModelState modelState) { }
