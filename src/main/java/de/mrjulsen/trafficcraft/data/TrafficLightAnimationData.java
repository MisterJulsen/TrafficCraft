package de.mrjulsen.trafficcraft.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.compat.TrafficLightMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

@SuppressWarnings("deprecation")
public class TrafficLightAnimationData {

    public static final int MAX_SECONDS = 999;
    public static final int MAX_TICKS = MAX_SECONDS * Constants.TPS;

    private List<TrafficLightColor> enabledColors = new ArrayList<>(TrafficLightColor.values().length);
    private int ticks = 0;
    private int id = 0;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrafficLightAnimationData other) {
            return ticks == other.ticks && id == other.id && enabledColors.size() == other.enabledColors.size() && enabledColors.stream().allMatch(x -> other.enabledColors.stream().anyMatch(y -> x == y));
        }
        return false;
    }

    public int getDurationTicks() {
        return this.ticks;
    }

    public double getDurationSeconds() {
        return (double)this.ticks / Constants.TPS;
    }

    public int getPhaseId() {
        return this.id;
    }


    public List<TrafficLightColor> getEnabledColors() {
        return this.enabledColors;
    }

    public void enableColors(Collection<TrafficLightColor> colors) {
        this.enabledColors.addAll(colors);
        this.enabledColors.stream().distinct().toList();
    }

    public void enableOnlyColors(Collection<TrafficLightColor> colors) {
        this.enabledColors.clear();
        this.enabledColors.addAll(colors);
        this.enabledColors.stream().distinct().toList();
    }

    public void disableColors(Collection<TrafficLightColor> colors) {
        colors.forEach(x -> enabledColors.removeIf(y -> x == y));
    }

    public void disableAll(Collection<TrafficLightColor> colors) {
        enabledColors.clear();
    }

    public void setDurationTicks(int ticks) {
        this.ticks = Mth.clamp(ticks, 0, MAX_TICKS);
    }

    public void setDurationSeconds(double seconds) {
        this.setDurationTicks((int)(seconds * Constants.TPS));
    }

    public void addDurationTicks(int amount) {
        this.ticks = Math.min(this.ticks + amount, MAX_TICKS);
    }

    public void addDurationSeconds(int amount) {
        this.addDurationTicks((int)(amount * Constants.TPS));
    }

    public void subDurationTicks(int amount) {
        this.ticks = Math.max(this.ticks - amount, 0);
    }

    public void subDurationSeconds(int amount) {
        this.subDurationTicks((int)(amount * Constants.TPS));
    }

    public void setPhaseId(int id) {
        this.id = id;
    }
    

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", this.getPhaseId());
        tag.putInt("ticks", this.getDurationTicks());
        tag.putIntArray("colors", this.getEnabledColors().stream().mapToInt(x -> x.getIndex()).toArray());
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        id = tag.getInt("id");
        ticks = tag.getInt("ticks");
        enabledColors = Arrays.stream(tag.getIntArray("colors")).mapToObj(x -> TrafficLightColor.getDirectionByIndex(x)).toList();

        // Backwards compatibility
        if (tag.contains("mode")) {
            enableOnlyColors(TrafficLightMode.getModeByIndex(tag.getInt("mode")).convertToColorList());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(ticks);
        buf.writeVarIntArray(this.getEnabledColors().stream().mapToInt(x -> x.getIndex()).toArray());
    }

    public static TrafficLightAnimationData fromBytes(FriendlyByteBuf buf) {
        TrafficLightAnimationData data = new TrafficLightAnimationData();
        data.setPhaseId(buf.readInt());
        data.setDurationTicks(buf.readInt());
        data.enableColors(Arrays.stream(buf.readVarIntArray()).mapToObj(x -> TrafficLightColor.getDirectionByIndex(x)).toList());
        return data;
    }
}
