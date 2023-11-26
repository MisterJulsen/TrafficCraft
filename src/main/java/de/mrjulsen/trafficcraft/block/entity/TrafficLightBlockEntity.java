package de.mrjulsen.trafficcraft.block.entity;

import java.util.List;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.TrafficLightControllerBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.data.Location;
import de.mrjulsen.trafficcraft.data.TrafficLightAnimationData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightBlockEntity extends ColoredBlockEntity {

    // Properties
    private int phaseId = 0;
    private int controlType = 0;
    private boolean powered = false;

    private TrafficLightSchedule schedule = new TrafficLightSchedule();
    private int ticker = 0;
    private long totalTicks = 0;
    private boolean running = true;

    // backwards compatibility
    @Deprecated private Location linkLocation = null;
    private boolean linkMigrated = false;

    protected TrafficLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrafficLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        this.phaseId = compound.getInt("phaseId");
        this.controlType = compound.getInt("controlType");
        this.powered = compound.getBoolean("powered");
        this.ticker = compound.getInt("ticker");
        this.totalTicks = compound.getLong("totalTicks");
        this.running = compound.getBoolean("running");
        this.schedule = new TrafficLightSchedule();
        this.schedule.fromNbt(compound.getCompound("schedule"));

        // backwards compatibility
        linkMigration(compound);
    }

    private void linkMigration(CompoundTag nbt) {
        if (nbt.contains("linkedTo")) {
            ModMain.LOGGER.warn("Traffic Light at position " + worldPosition.toShortString() + " contains deprecated link data. Trying to convert it.");            
            linkLocation = Location.fromNbt(nbt.getCompound("linkedTo"));
            linkMigrated = false;
            return;
        }
        linkMigrated = true;        
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("phaseId", phaseId);
        tag.putBoolean("powered", powered);
        tag.putInt("controlType", controlType);
        tag.putInt("ticker", ticker);
        tag.putLong("totalTicks", ticker);
        tag.putBoolean("running", running);
        tag.put("schedule", schedule.toNbt());
        
        // backwards compatibility
        if (!linkMigrated && this.linkLocation != null) {
            tag.put("linkedTo", linkLocation.toNbt());
        }
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
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 3, 512);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        
        // backwards compatibility       
        linkMigrationCheck(level, pos, state);

        if (running && this.getControlType() == TrafficLightControlType.OWN_SCHEDULE) {
            List<TrafficLightAnimationData> stateData = schedule.shouldChange(ticker);

            if (stateData == null) {
                ticker = 0;
                if (!schedule.isLoop()) {
                    setRunning(false);
                }
                return;
            } else if (stateData.size() >= 0) {
                for (TrafficLightAnimationData entry : stateData) {
                    TrafficLightMode mode = entry.getMode();
                    if (mode != null) {
                        level.setBlockAndUpdate(pos, state.setValue(TrafficLightBlock.MODE, mode));                    
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

        if (level.isLoaded(linkLocation.getLocationAsBlockPos())) {
            if (level.getBlockState(linkLocation.getLocationAsBlockPos()).getBlock() instanceof TrafficLightControllerBlock &&
                level.getBlockEntity(linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity
            ) {
                blockEntity.addTrafficLightLocation(linkLocation);
            }
            linkMigrated = true;
            return;
        }        
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrafficLightBlockEntity blockEntity) {
        blockEntity.tick(level, pos, state);
    }


    /* GETTERS AND SETTERS */

    public void setPhaseId(int id)
    {
        this.phaseId = id;
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean setControlType(int controlTypeIndex) {
        if (controlTypeIndex >= TrafficLightControlType.values().length)
            return false;

        this.controlType = controlTypeIndex;
        BlockEntityUtil.sendUpdatePacket(this);
        return true;
    }

    public void setControlType(TrafficLightControlType controlType) {
        this.controlType = controlType.getIndex();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public void setSchedule(TrafficLightSchedule schedule) {
        this.schedule = schedule;
        BlockEntityUtil.sendUpdatePacket(this);
    }
    
    public int getPhaseId() {
        return this.phaseId;
    }

    public int getControlTypeAsInt() {
        return this.controlType;
    }

    public TrafficLightControlType getControlType() {
        return TrafficLightControlType.getControlTypeByIndex(this.controlType);
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
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public void startSchedule(boolean forceRestart) {
        if (this.controlType == TrafficLightControlType.OWN_SCHEDULE.getIndex() && (forceRestart || !this.isFirstIteration())) {
            this.ticker = 0;
            this.totalTicks = 0;
            this.running = true;
        }
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public void stopSchedule() {
        this.running = false;
        this.totalTicks = 0;
        this.ticker = 0;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean isFirstIteration() {
        return this.totalTicks == this.ticker;
    }

    public void setPowered(boolean b) {
        this.powered = b;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean isPowered() {
        return this.powered;
    }
}
