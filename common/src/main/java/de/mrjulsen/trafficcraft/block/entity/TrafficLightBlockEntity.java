package de.mrjulsen.trafficcraft.block.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.mcdragonlib.core.Location;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.TrafficLightControllerBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightBlockEntity extends ColoredBlockEntity {

    private static final String NBT_PHASE_ID = "phaseId";
    private static final String NBT_CONTROL_TYPE = "controlType";
    private static final String NBT_POWERED = "powered";
    private static final String NBT_TICKS = "ticks";
    private static final String NBT_TOTAL_TICKS = "totalTicks";
    private static final String NBT_RUNNING = "running";
    private static final String NBT_SCHEDULE = "schedule";
    private static final String NBT_ICON = "icon";
    private static final String NBT_TYPE = "type";
    private static final String NBT_COLOR_SLOTS = "colorSlots";
    private static final String NBT_ENABLED_COLORS = "enabledColors";
    @Deprecated private static final String NBT_LINKED_TO = "linkedTo";

    // Properties
    private int phaseId = 0;
    private TrafficLightControlType controlType = TrafficLightControlType.STATIC;
    private TrafficLightIcon icon = TrafficLightIcon.NONE;
    private TrafficLightType type = TrafficLightType.CAR;
    private final TrafficLightColor[] colorSlots = new TrafficLightColor[] {
        TrafficLightColor.RED,
        TrafficLightColor.YELLOW,
        TrafficLightColor.GREEN
    };
    private final Collection<TrafficLightColor> enabledColors = new ArrayList<>();
    private boolean powered = false;

    private TrafficLightSchedule schedule = new TrafficLightSchedule();
    private int ticker = 0;
    private long totalTicks = 0;
    private boolean running = true;

    /** @deprecated Backwards compatibility only! */ @Deprecated private Location linkLocation = null;
    private boolean linkMigrated = false;

    protected TrafficLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrafficLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);

        this.phaseId = tag.getInt(NBT_PHASE_ID);
        this.controlType = TrafficLightControlType.getControlTypeByIndex(tag.getTagType(NBT_COLOR_SLOTS) == Tag.TAG_INT ? (byte)tag.getInt(NBT_CONTROL_TYPE) : tag.getByte(NBT_CONTROL_TYPE));
        this.powered = tag.getBoolean(NBT_POWERED);
        this.ticker = tag.getInt(NBT_TICKS);
        this.totalTicks = tag.getLong(NBT_TOTAL_TICKS);
        this.running = tag.getBoolean(NBT_RUNNING);
        this.schedule = new TrafficLightSchedule();
        this.schedule.fromNbt(tag.getCompound(NBT_SCHEDULE));
        this.icon = TrafficLightIcon.getIconByIndex(tag.getByte(NBT_ICON));
        this.type = TrafficLightType.getTypeByIndex(tag.getByte(NBT_TYPE));
        int[] colorSlots = tag.getIntArray(NBT_COLOR_SLOTS);
        for (int i = 0; i < colorSlots.length && i < this.colorSlots.length; i++) {
            this.colorSlots[i] = TrafficLightColor.getColorByIndex((byte)colorSlots[i]);
        }
        this.enabledColors.clear();
        this.enabledColors.addAll(tag.getList(NBT_ENABLED_COLORS, Tag.TAG_BYTE).stream().map(x -> TrafficLightColor.getColorByIndex(((ByteTag)x).getAsByte())).toList());

        // backwards compatibility
        linkMigration(tag);
    }

    @SuppressWarnings("deprecation")
    private void linkMigration(CompoundTag nbt) {
        if (nbt.contains(NBT_LINKED_TO)) {
            TrafficCraft.LOGGER.warn("Traffic Light at position " + worldPosition.toShortString() + " contains deprecated link data. Trying to convert it.");            
            linkLocation = Location.fromNbtAsInt(nbt.getCompound(NBT_LINKED_TO));
            linkMigrated = false;
            return;
        }
        linkMigrated = true;        
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(NBT_PHASE_ID, phaseId);
        tag.putBoolean(NBT_POWERED, powered);
        tag.putByte(NBT_CONTROL_TYPE, controlType.getIndex());
        tag.putInt(NBT_TICKS, ticker);
        tag.putLong(NBT_TOTAL_TICKS, ticker);
        tag.putBoolean(NBT_RUNNING, running);
        tag.put(NBT_SCHEDULE, schedule.toNbt());
        tag.putIntArray(NBT_COLOR_SLOTS, Arrays.stream(colorSlots).mapToInt(x -> x.getIndex()).toArray());
        tag.putByte(NBT_ICON, icon.getIndex());
        tag.putByte(NBT_TYPE, type.getIndex());
        ListTag enabledColorsTag = new ListTag();
        enabledColorsTag.addAll(enabledColors.stream().map(x -> ByteTag.valueOf(x.getIndex())).toList());
        tag.put(NBT_ENABLED_COLORS, enabledColorsTag);
        
        // backwards compatibility
        if (!linkMigrated && this.linkLocation != null) {
            tag.put(NBT_LINKED_TO, linkLocation.toNbt());
        }
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        
        // backwards compatibility       
        linkMigrationCheck(level, pos, state);

        if (running && this.getControlType() == TrafficLightControlType.OWN_SCHEDULE) {
            List<TrafficLightScheduleEntryData> stateData = schedule.shouldChange(ticker);

            if (stateData == null) { // OOB: End of schedule reached.
                ticker = 0;
                if (!schedule.isLoop()) {
                    setRunning(false);
                }
                return;
            } else if (stateData.size() >= 0) {
                for (TrafficLightScheduleEntryData entry : stateData) {
                    Collection<TrafficLightColor> colors = entry.getEnabledColors();
                    if (colors != null) {
                        enableOnlyColors(colors);
                    }
                }
            }
            ticker++;
            totalTicks++;
        }

        if (isPowered() && !level.hasNeighborSignal(pos)) {                
            this.setPowered(false);
        }
    }

    private void linkMigrationCheck(Level level, BlockPos pos, BlockState state) {
        if (linkMigrated) {
            return;
        }

        if (linkLocation == null) {
            linkMigrated = true;
            return;
        }

        if (level.isLoaded(linkLocation.getLocationBlockPos())) {
            if (level.getBlockState(linkLocation.getLocationBlockPos()).getBlock() instanceof TrafficLightControllerBlock &&
                level.getBlockEntity(linkLocation.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity
            ) {
                blockEntity.addTrafficLightLocation(new Location(pos.getX(), pos.getY(), pos.getZ(), level.dimension().location().toString()));
            }
            linkMigrated = true;
            return;
        }        
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrafficLightBlockEntity blockEntity) {
        blockEntity.tick(level, pos, state);
    }


    /* GETTERS AND SETTERS */

    public void setPhaseId(int id) {
        this.phaseId = id;
        notifyUpdate();
    }

    public void setControlType(TrafficLightControlType controlType) {
        this.controlType = controlType;
        notifyUpdate();
    }

    public void setSchedule(TrafficLightSchedule schedule) {
        this.schedule = schedule;
        notifyUpdate();
    }

    public void setIcon(TrafficLightIcon icon) {
        this.icon = icon;
        notifyUpdate();
    }

    public boolean setColorToSlot(int index, TrafficLightColor color) {
        if (index < 0 || index >= colorSlots.length) {
            return false;
        }
        this.colorSlots[index] = color;
        notifyUpdate();
        return true;
    }

    public void setColorSlots(TrafficLightColor[] colorSlots) {
        for (int i = 0; i < colorSlots.length && i < getColorSlotCount(); i++) {
            this.colorSlots[i] = colorSlots[i];
        }
        notifyUpdate();
    }

    public void enableColors(Collection<TrafficLightColor> colors) {
        this.enabledColors.addAll(colors);
        this.enabledColors.stream().distinct().toList();
        notifyUpdate();
    }

    public void enableOnlyColors(Collection<TrafficLightColor> colors) {
        this.enabledColors.clear();
        this.enabledColors.addAll(colors);
        this.enabledColors.stream().distinct().toList();
        notifyUpdate();
    }

    public void disableColors(Collection<TrafficLightColor> colors) {
        colors.forEach(x -> enabledColors.removeIf(y -> x == y));
        notifyUpdate();
    }

    public void disableAll(Collection<TrafficLightColor> colors) {
        enabledColors.clear();
        notifyUpdate();
    }

    public void setType(TrafficLightType type) {
        this.type = type;
        notifyUpdate();
    }


    

    public Collection<TrafficLightColor> getEnabledColors() {
        return this.enabledColors;
    }

    public boolean isColorEnabled(TrafficLightColor color, boolean allowSimilar) {
        return this.enabledColors.stream().anyMatch(x -> (allowSimilar && x.isSimilar(color)) || x == color);
    }
    
    public int getPhaseId() {
        return this.phaseId;
    }

    public TrafficLightControlType getControlType() {
        return this.controlType;
    }

    public TrafficLightColor[] getColorSlots() {
        return this.colorSlots;
    }

    public TrafficLightIcon getIcon() {
        return this.icon;
    }

    public TrafficLightType getTLType() {
        return this.type;
    }

    public TrafficLightColor getColorOfSlot(int index) {
        return index >= 0 && index < colorSlots.length ? colorSlots[index] : TrafficLightColor.NONE;
    }

    public int getColorSlotCount() {
        return colorSlots.length;
    }

    public TrafficLightSchedule getSchedule() {
        return this.schedule;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean b) {
        if (b && !this.running) {
            this.ticker = 0;
            this.totalTicks = 0;
        }

        this.running = b;
        notifyUpdate();
    }

    public void startSchedule(boolean forceRestart) {
        if (this.controlType == TrafficLightControlType.OWN_SCHEDULE && (forceRestart || !this.isFirstIteration())) {
            this.ticker = 0;
            this.totalTicks = 0;
            this.running = true;
        }
        notifyUpdate();
    }

    public void stopSchedule() {
        this.running = false;
        this.totalTicks = 0;
        this.ticker = 0;
        notifyUpdate();
    }

    public boolean isFirstIteration() {
        return this.totalTicks == this.ticker;
    }

    public void setPowered(boolean b) {
        this.powered = b;
        notifyUpdate();
    }

    public boolean isPowered() {
        return this.powered;
    }
}
