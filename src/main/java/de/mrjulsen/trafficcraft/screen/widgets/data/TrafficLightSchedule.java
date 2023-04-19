package de.mrjulsen.trafficcraft.screen.widgets.data;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.trafficcraft.block.properties.TrafficLightMode;
import de.mrjulsen.trafficcraft.block.properties.TrafficLightTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class TrafficLightSchedule {
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
        int d = 0;
        for (TrafficLightAnimationData entry : entries) {
            d += entry.getDurationTicks();
        }
        return d;
    }

    public List<Integer> shouldChange(int currentTick) {
        List<Integer> i = new ArrayList<Integer>();
        int keyTime = 0;
        int index = 0;
        for (TrafficLightAnimationData entry : entries) {
            if (currentTick == keyTime) {
                i.add(index);
            }
            keyTime += entry.getDurationTicks(); 
            index++; 

            if (currentTick < keyTime) {
                return i.size() > 0 ? i : List.of(-1);
            }
        }
        return i.size() > 0 ? i : List.of(-2);
    }

    public TrafficLightMode getModeForUpdate(int index) {
        if (index < 0 || index >= entries.size())
            return null;

        return entries.get(index).getMode();
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

        tag.putBoolean("loop", loop);
        tag.putInt("trigger", trigger.getIndex());
        tag.put("entries", listTag);
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        loop = tag.getBoolean("loop");
        trigger = TrafficLightTrigger.getTriggerByIndex(tag.getInt("trigger"));
        ListTag listTag = tag.getList("entries", 10); // 10 ist der ID-Typ für CompoundTags

        for (int i = 0; i < listTag.size(); i++) {
            TrafficLightAnimationData data = new TrafficLightAnimationData();
            data.fromNbt(listTag.getCompound(i));
            entries.add(data);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(loop);
        buf.writeInt(trigger.getIndex());
        buf.writeInt(entries.size());
        for (TrafficLightAnimationData data : entries) {
            data.toBytes(buf);
        }
    }

    public static TrafficLightSchedule fromBytes(FriendlyByteBuf buf) {
        TrafficLightSchedule schedule = new TrafficLightSchedule();
        schedule.setLoop(buf.readBoolean());
        schedule.setTrigger(TrafficLightTrigger.getTriggerByIndex(buf.readInt()));
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            schedule.entries.add(TrafficLightAnimationData.fromBytes(buf));
        }
        return schedule;
    }
}
