package de.mrjulsen.trafficcraft.block.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.data.Location;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightControllerBlockEntity extends BlockEntity {

    private static final String NBT_TRAFFIC_LIGHT_LOCATIONS = "LinkedTrafficLights";

    // Properties
    private List<TrafficLightSchedule> schedules = new ArrayList<>();
    private int ticks = 0;
    private long totalTicks = 0;
    private boolean running = true;
    private boolean powered = false;
    private List<Location> trafficLightLocations = new ArrayList<>();

    // ticking
    private Map<Integer, TrafficLightMode> modes = new HashMap<>();

    protected TrafficLightControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrafficLightControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        this.ticks = compound.getInt("ticks");
        this.running = compound.getBoolean("running");
        this.totalTicks = compound.getLong("totalTicks");
        this.powered = compound.getBoolean("powered");

        ListTag listTag = compound.getList("schedules", Tag.TAG_COMPOUND);
        schedules.clear();
        for (int i = 0; i < listTag.size(); i++) {
            TrafficLightSchedule data = new TrafficLightSchedule();
            data.fromNbt(listTag.getCompound(i));
            schedules.add(data);
        }

        ListTag modesList = compound.getList("modes", Tag.TAG_COMPOUND);
        modes.clear();
        for (int i = 0; i < modesList.size(); i++) {
            CompoundTag c = modesList.getCompound(i);
            int key = c.getInt("key");
            int value = compound.getInt("value");
            modes.put(key, TrafficLightMode.getModeByIndex(value));
        }

        ListTag trafficLightsList = compound.getList(NBT_TRAFFIC_LIGHT_LOCATIONS, Tag.TAG_COMPOUND);
        trafficLightLocations.clear();
        for (int i = 0; i < modesList.size(); i++) {
            Location loc = Location.fromNbt(trafficLightsList.getCompound(i));
            trafficLightLocations.add(loc);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {       
        ListTag listTag = new ListTag();
        for (TrafficLightSchedule data : schedules) {
            listTag.add(data.toNbt());
        }

        ListTag modesTag = new ListTag();        
        for (Map.Entry<Integer, TrafficLightMode> entry : modes.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putInt("key", entry.getKey());
            c.putInt("value", entry.getValue().getIndex());
            modesTag.add(c);
        }

        ListTag trafficLightsList = new ListTag();
        for (Location loc : trafficLightLocations) {
            trafficLightsList.add(loc.toNbt());
        }

        tag.putInt("ticks", ticks);
        tag.putLong("totalTicks", totalTicks);
        tag.putBoolean("powered", powered);
        tag.putBoolean("running", running);
        tag.put("schedules", listTag);
        tag.put("modes", modesTag);
        tag.put(NBT_TRAFFIC_LIGHT_LOCATIONS, trafficLightsList);
        super.saveAdditional(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        this.load(pkt.getTag());
    }

    private void instancedTick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            if (running) {
                TrafficLightSchedule schedule = this.getFirstOrMainSchedule();
                List<Integer> i = schedule.shouldChange(ticks);
    
                if (i != null && i.get(0) == -2) {
                    ticks = 0;
                    if (!schedule.isLoop()) {
                        this.running = false; 
                        BlockEntityUtil.sendUpdatePacket(this);
                    }
                    return;
                } else if (i != null && i.get(0) >= 0) {
                    for (int x : i) {
                        TrafficLightMode mode = schedule.getModeForUpdate(x);
                        int phaseId = schedule.getPhaseId(x);
                        if (mode != null && phaseId != Integer.MAX_VALUE) {
                            if (modes.containsKey(phaseId)) {
                                modes.remove(phaseId);
                            }                    
    
                            modes.put(phaseId, mode);
                        }
                    }
                }
                ticks++;
                totalTicks++;
            }

            if (!level.hasNeighborSignal(pos)) {                
                this.setPowered(false);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrafficLightControllerBlockEntity blockEntity) {
        blockEntity.instancedTick(level, pos, state);
    }


    /* GETTERS AND SETTERS */
   
    public List<TrafficLightSchedule> getSchedules() {
        return this.schedules;
    }

    public TrafficLightSchedule getFirstOrMainSchedule() {
        if (this.schedules.size() > 0) {
            return this.schedules.get(0);
        }

        return new TrafficLightSchedule();
    }

    public void setFirstOrMainSchedule(TrafficLightSchedule schedule) {
        if (this.schedules.size() > 0)
            this.schedules.remove(0);

        this.schedules.add(0, schedule);
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public void setSchedules(List<TrafficLightSchedule> schedules) {
        this.schedules.clear();
        this.schedules = schedules;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public int getCurrentTick() {
        return ticks;
    }

    public void setCurrentTick(int t) {
        this.ticks = t;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean b) {
        this.running = b;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public TrafficLightMode getModeForPhaseId(int phaseId) {
        if (!modes.containsKey(phaseId))
            return null;

        return modes.get(phaseId);
    }

    public void startSchedule(boolean forceRestart) {
        if (forceRestart || !this.isFirstIteration()) {
            this.totalTicks = 0;
            this.ticks = 0;
            this.running = true;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
        }
    }

    public boolean hasSomethingToDo(int phaseId, TrafficLightMode currentMode) {
        return modes.containsKey(phaseId) && modes.get(phaseId) != currentMode;
    }

    public boolean isFirstIteration() {
        return this.totalTicks == this.ticks;
    }

    public void setPowered(boolean b) {
        this.powered = b;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean isPowered() {
        return this.powered;
    }

    public List<Location> getTrafficLightLocations() {
        return trafficLightLocations;
    }

    public void addTrafficLightLocation(Location loc) {
        if (!trafficLightLocations.contains(loc)) {
            trafficLightLocations.add(loc);
            setChanged();
            BlockEntityUtil.sendUpdatePacket(this);
        }
    }

    public void removeTrafficLightLocation(Location loc) {
        trafficLightLocations.removeIf(x -> x.equals(loc));
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }
}
