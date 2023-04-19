package de.mrjulsen.trafficcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class Location {
    public int x;
    public int y;
    public int z;
    public String dimension;
    private BlockPos blockPos;

    private Location() {}

    public Location(int x, int y, int z, String dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.generateBlockPos();
    }

    public BlockPos getLocationAsBlockPos() {
        return blockPos;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putString("dim", dimension);

        return tag;
    }

    private void generateBlockPos() {
        this.blockPos = new BlockPos(this.x, this.y, this.z);
    }

    public static Location fromNbt(CompoundTag tag) {
        Location loc = new Location();
        loc.loadFromNbt(tag);
        return loc;
    }

    public void loadFromNbt(CompoundTag tag) {
        this.x = tag.getInt("x");
        this.y = tag.getInt("y");
        this.z = tag.getInt("z");
        this.dimension = tag.getString("dim");
        this.generateBlockPos();
    }
}
