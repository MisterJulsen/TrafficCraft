package de.mrjulsen.trafficcraft.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLibConstants;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public class TrafficLightAnimationData {

    private static final String NBT_ID = "id";
    private static final String NBT_TICKS = "ticks";
    private static final String NBT_COLOR = "color";
    @Deprecated private static final String NBT_MODE = "mode";

    public static final int MAX_SECONDS = 999;
    public static final int MAX_TICKS = MAX_SECONDS * DragonLibConstants.TPS;

    private List<TrafficLightColor> enabledColors = new ArrayList<>(TrafficLightColor.values().length);
    private int ticks = 0;
    private int id = 0;

    public TrafficLightAnimationData copy() {
        TrafficLightAnimationData data = new TrafficLightAnimationData();
        data.enabledColors = new ArrayList<>(enabledColors);
        data.ticks = ticks;
        data.id = id;
        return data;
    }

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
        return (double)this.ticks / DragonLibConstants.TPS;
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
        this.setDurationTicks((int)(seconds * DragonLibConstants.TPS));
    }

    public void addDurationTicks(int amount) {
        this.ticks = Math.min(this.ticks + amount, MAX_TICKS);
    }

    public void addDurationSeconds(int amount) {
        this.addDurationTicks((int)(amount * DragonLibConstants.TPS));
    }

    public void subDurationTicks(int amount) {
        this.ticks = Math.max(this.ticks - amount, 0);
    }

    public void subDurationSeconds(int amount) {
        this.subDurationTicks((int)(amount * DragonLibConstants.TPS));
    }

    public void setPhaseId(int id) {
        this.id = id;
    }
    

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_ID, this.getPhaseId());
        tag.putInt(NBT_TICKS, this.getDurationTicks());
        tag.putIntArray(NBT_COLOR, this.getEnabledColors().stream().mapToInt(x -> x.getIndex()).toArray());
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        id = tag.getInt(NBT_ID);
        ticks = tag.getInt(NBT_TICKS);
        byte[] bArr = tag.getByteArray(NBT_COLOR);
        List<TrafficLightColor> colors = new ArrayList<>(bArr.length);
        for (int i = 0; i < bArr.length; i++) {
            colors.add(TrafficLightColor.getDirectionByIndex(bArr[i]));
        }
        enabledColors = colors;

        // Backwards compatibility
        migrateData(tag);
    }

    @SuppressWarnings("deprecation")
    private void migrateData(CompoundTag nbt) {
        if (nbt.contains(NBT_MODE)) {
            enableOnlyColors(de.mrjulsen.trafficcraft.block.data.compat.TrafficLightMode.getModeByIndex(nbt.getInt(NBT_MODE)).convertToColorList());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(ticks);
        TrafficLightColor[] cArr = this.getEnabledColors().toArray(TrafficLightColor[]::new);
        byte[] bArr = new byte[cArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = cArr[i].getIndex();
        }
        buf.writeByteArray(bArr);
    }

    public static TrafficLightAnimationData fromBytes(FriendlyByteBuf buf) {
        TrafficLightAnimationData data = new TrafficLightAnimationData();
        data.setPhaseId(buf.readInt());
        data.setDurationTicks(buf.readInt());
        byte[] bArr = buf.readByteArray();
        Collection<TrafficLightColor> colors = new ArrayList<>(bArr.length);
        for (int i = 0; i < bArr.length; i++) {
            colors.add(TrafficLightColor.getDirectionByIndex(bArr[i]));
        }
        data.enableColors(colors);
        return data;
    }
}
