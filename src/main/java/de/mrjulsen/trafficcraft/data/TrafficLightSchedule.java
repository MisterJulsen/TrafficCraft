package de.mrjulsen.trafficcraft.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class TrafficLightSchedule {

    private static final String NBT_LOOP = "loop";
    private static final String NBT_ENTRIES = "entries";
    private static final String NBT_TRIGGER = "trigger";
    
    private List<TrafficLightAnimationData> entries = new ArrayList<>();
    private boolean loop = true;
    private TrafficLightTrigger trigger = TrafficLightTrigger.NONE;
    
    public List<TrafficLightAnimationData> getEntries() {
        return this.entries;
    }

    public boolean isLoop() {
        return this.loop;
    }

    public TrafficLightTrigger getTrigger() {
        return this.trigger;
    }

    public void setLoop(boolean b)  {
        this.loop = b;
    }

    public void setTrigger(TrafficLightTrigger trigger) {
        this.trigger = trigger;
    }

    public int getTotalDurationTicks() {
        return entries.stream().mapToInt(x -> x.getDurationTicks()).sum();
    }

    /**
     * Check if there is something to change.
     * @param currentTick
     * @return List of phaseIDs. Returns {@code null} if {@code currentTick} is out of bounds. Returns an empty list, if thee is nothing to change. Returns a list containing the indices of states to change, if there is something to change.
     */
    public List<TrafficLightAnimationData> shouldChange(int currentTick) {
        List<TrafficLightAnimationData> changeEntries = new ArrayList<>();
        int keyTime = 0;

        for (TrafficLightAnimationData entry : entries) {
            if (keyTime == currentTick) {
                changeEntries.add(entry);
            }
            keyTime += entry.getDurationTicks();

            if (currentTick < keyTime) {
                return changeEntries.stream().distinct().toList();
            }
        }

        return changeEntries.size() <= 0 ? null : changeEntries.stream().distinct().toList();
    }
    
    public Collection<TrafficLightColor> getColorsForUpdate(int index) {
        if (index < 0 || index >= entries.size())
            return Collections.emptyList();

        return entries.get(index).getEnabledColors();
    }

    public int getPhaseId(int index) {
        if (index < 0 || index >= entries.size())
            return Integer.MAX_VALUE;

        return entries.get(index).getPhaseId();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        
        ListTag listTag = new ListTag();

        for (TrafficLightAnimationData data : entries) {
            listTag.add(data.toNbt());
        }

        tag.putBoolean(NBT_LOOP, loop);
        tag.putByte(NBT_TRIGGER, trigger.getIndex());
        tag.put(NBT_ENTRIES, listTag);
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        loop = tag.getBoolean(NBT_LOOP);
        trigger = TrafficLightTrigger.getTriggerByIndex(tag.getTagType(NBT_TRIGGER) == Tag.TAG_INT ? (byte)tag.getInt(NBT_TRIGGER) : tag.getByte(NBT_TRIGGER));
        ListTag listTag = tag.getList(NBT_ENTRIES, Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            TrafficLightAnimationData data = new TrafficLightAnimationData();
            data.fromNbt(listTag.getCompound(i));
            entries.add(data);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(loop);
        buf.writeByte(trigger.getIndex());
        buf.writeInt(entries.size());
        for (TrafficLightAnimationData data : entries) {
            data.toBytes(buf);
        }
    }

    public static TrafficLightSchedule fromBytes(FriendlyByteBuf buf) {
        TrafficLightSchedule schedule = new TrafficLightSchedule();
        schedule.setLoop(buf.readBoolean());
        schedule.setTrigger(TrafficLightTrigger.getTriggerByIndex(buf.readByte()));
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            schedule.entries.add(TrafficLightAnimationData.fromBytes(buf));
        }
        return schedule;
    }
}
