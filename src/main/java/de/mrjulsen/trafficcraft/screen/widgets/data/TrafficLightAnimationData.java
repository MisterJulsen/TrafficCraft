package de.mrjulsen.trafficcraft.screen.widgets.data;

import de.mrjulsen.trafficcraft.block.properties.TrafficLightMode;
import de.mrjulsen.trafficcraft.screen.widgets.IListEntryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class TrafficLightAnimationData implements IListEntryData {

    private static final byte TPS = 20;

    public static final int MAX_SECONDS = 999;
    public static final int MAX_TICKS = MAX_SECONDS * TPS;

    private TrafficLightMode mode = TrafficLightMode.OFF;
    private int ticks = 0;
    private int id = 0;

    public TrafficLightMode getMode() {
        return this.mode;
    }

    public int getDurationTicks() {
        return this.ticks;
    }

    public double getDurationSeconds() {
        return (double)this.ticks / TPS;
    }

    public int getPhaseId() {
        return this.id;
    }


    public void setMode(TrafficLightMode mode) {
        this.mode = mode;
    }

    public boolean setTrafficLightMode(int index) {
        if (index >= TrafficLightMode.values().length)
            return false;

        this.mode = TrafficLightMode.getModeByIndex(index);
        return true;
    }

    public void setDurationTicks(int ticks) {
        this.ticks = Mth.clamp(ticks, 0, MAX_TICKS);
    }

    public void setDurationSeconds(double seconds) {
        this.setDurationTicks((int)(seconds * TPS));
    }

    public void addDurationTicks(int amount) {
        this.ticks = Math.min(this.ticks + amount, MAX_TICKS);
    }

    public void addDurationSeconds(int amount) {
        this.addDurationTicks((int)(amount * TPS));
    }

    public void subDurationTicks(int amount) {
        this.ticks = Math.max(this.ticks - amount, 0);
    }

    public void subDurationSeconds(int amount) {
        this.subDurationTicks((int)(amount * TPS));
    }

    public void setPhaseId(int id) {
        this.id = id;
    }
    

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", this.getPhaseId());
        tag.putInt("ticks", this.getDurationTicks());
        tag.putInt("mode", this.getMode().getIndex());
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        id = tag.getInt("id");
        ticks = tag.getInt("ticks");
        mode = TrafficLightMode.getModeByIndex(tag.getInt("mode"));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(ticks);
        buf.writeInt(mode.getIndex());
    }

    public static TrafficLightAnimationData fromBytes(FriendlyByteBuf buf) {
        TrafficLightAnimationData data = new TrafficLightAnimationData();
        data.setPhaseId(buf.readInt());
        data.setDurationTicks(buf.readInt());
        data.setMode(TrafficLightMode.getModeByIndex(buf.readInt()));
        return data;
    }
}
