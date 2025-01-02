package de.mrjulsen.trafficcraft.data;

import java.util.Collection;
import java.util.Collections;

import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.util.OrderedArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class TrafficLightSchedule implements INBTSerializable {

    private static final String NBT_LOOP = "loop";
    private static final String NBT_ENTRIES = "entries";
    private static final String NBT_TRIGGER = "trigger";
    
    private OrderedArrayList<TrafficLightScheduleEntryData> entries = new OrderedArrayList<>();
    private boolean loop = true;
    private TrafficLightTrigger trigger = TrafficLightTrigger.NONE;

    public TrafficLightSchedule copy() {
        TrafficLightSchedule schedule = new TrafficLightSchedule();
        schedule.entries.addAll(entries.stream().map(x -> x.copy()).toList());
        schedule.loop = loop;
        schedule.trigger = trigger;
        return schedule;
    }
    
    public OrderedArrayList<TrafficLightScheduleEntryData> getEntries() {
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
    public OrderedArrayList<TrafficLightScheduleEntryData> shouldChange(int currentTick) {
        OrderedArrayList<TrafficLightScheduleEntryData> changeEntries = new OrderedArrayList<>();
        int keyTime = 0;

        for (TrafficLightScheduleEntryData entry : entries) {
            keyTime += entry.getDurationTicks();
            if (keyTime == currentTick) {
                changeEntries.add(entry);
            }

            if (currentTick < keyTime) {
                return new OrderedArrayList<>(changeEntries.stream().distinct().toList());
            }
        }

        return changeEntries.size() <= 0 ? null : new OrderedArrayList<>(changeEntries.stream().distinct().toList());
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
        for (TrafficLightScheduleEntryData data : entries) {
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
        
        // START Backward compatibility
        double lastTime = -1;
        boolean migration = false;
        // END Backward compatibility

        for (int i = 0; i < listTag.size(); i++) {
            TrafficLightScheduleEntryData data = new TrafficLightScheduleEntryData();            
            data.fromNbt(listTag.getCompound(i));

            // START Backward compatibility
            if (migration = data.shouldMigrate(listTag.getCompound(i))) {                
                double lTime = data.getDurationSeconds();
                if (lastTime >= 0) {
                    data.setDurationSeconds(lastTime);
                }
                lastTime = lTime;
            }            
            // END Backward compatibility

            entries.add(data);
        }
        
        // START Backward compatibility
        if (migration && entries.size() > 0) {
            entries.get(0).setDurationSeconds(lastTime);
        }
        // END Backward compatibility
        
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(loop);
        buf.writeByte(trigger.getIndex());
        buf.writeInt(entries.size());
        for (TrafficLightScheduleEntryData data : entries) {
            data.toBytes(buf);
        }
    }

    public static TrafficLightSchedule fromBytes(FriendlyByteBuf buf) {
        TrafficLightSchedule schedule = new TrafficLightSchedule();
        schedule.setLoop(buf.readBoolean());
        schedule.setTrigger(TrafficLightTrigger.getTriggerByIndex(buf.readByte()));
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            schedule.entries.add(TrafficLightScheduleEntryData.fromBytes(buf));
        }
        return schedule;
    }

    @Override
    public CompoundTag serializeNbt() {
        return toNbt();
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        fromNbt(nbt);
    }
}
