package de.mrjulsen.trafficcraft.block.entity;

import java.util.List;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.properties.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.properties.TrafficLightMode;
import de.mrjulsen.trafficcraft.screen.widgets.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import de.mrjulsen.trafficcraft.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightBlockEntity extends BlockEntity {

    // Properties
    private int phaseId = 0;
    private int controlType = 0;
    private Location linkLocation;
    private boolean powered = false;

    private TrafficLightSchedule schedule = new TrafficLightSchedule();
    private int ticker = 0;
    private long totalTicks = 0;
    private boolean running = true;

    protected TrafficLightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public TrafficLightBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        this.phaseId = compound.getInt("phaseId");
        this.controlType = compound.getInt("controlType");
        this.powered = compound.getBoolean("powered");
        this.ticker = compound.getInt("ticker");
        this.totalTicks = compound.getLong("totalTicks");
        this.running = compound.getBoolean("running");
        this.schedule = new TrafficLightSchedule();
        this.schedule.fromNbt(compound.getCompound("schedule"));
        if (compound.contains("linkedTo")) {
            this.linkLocation = Location.fromNbt(compound.getCompound("linkedTo"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        tag.putInt("phaseId", phaseId);
        tag.putBoolean("powered", powered);
        tag.putInt("controlType", controlType);
        tag.putInt("ticker", ticker);
        tag.putLong("totalTicks", ticker);
        tag.putBoolean("running", running);
        tag.put("schedule", schedule.toNbt());
        if (this.linkLocation != null) {
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
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        
        if (!level.isClientSide) {
            if (running && this.getControlType() == TrafficLightControlType.OWN_SCHEDULE) {
                List<Integer> i = schedule.shouldChange(ticker);
    
                if (i != null && i.get(0) == -2) {
                    ticker = 0;
                    if (!schedule.isLoop()) {
                        this.running = false;
                        BlockEntityUtil.sendUpdatePacket(this); 
                    }
                    return;
                } else if (i != null && i.get(0) >= 0) {
                    for (int x : i) {
                        TrafficLightMode mode = this.schedule.getModeForUpdate(x);
                        if (mode != null) {
                            level.setBlockAndUpdate(pos, state.setValue(TrafficLightBlock.MODE, mode));                    
                        }
                    }
                }
                ticker++;
                totalTicks++;
            } else if (this.getControlType() == TrafficLightControlType.REMOTE) {
                if (linkLocation != null && level.isLoaded(this.linkLocation.getLocationAsBlockPos())) {
                    if (level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity controller) {
                        if (controller.isRemoved() || !controller.isRunning()) {
                            return;
                        }
    
                        if (controller.hasSomethingToDo(phaseId, state.getValue(TrafficLightBlock.MODE))) {
                            TrafficLightMode mode = controller.getModeForPhaseId(phaseId);
                            if (mode != null) {
                                level.setBlockAndUpdate(pos, state.setValue(TrafficLightBlock.MODE, mode));                    
                            }
                        }
                    }
                }
            }

            if (!level.hasNeighborSignal(pos)) {                
                this.setPowered(false);
            }

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

    public void linkTo(BlockPos pos, String dimension) {
        this.linkLocation = new Location(pos.getX(), pos.getY(), pos.getZ(), dimension);
        BlockEntityUtil.sendUpdatePacket(this);
    }    

    public void clearLink() {
        this.linkLocation = null;
        BlockEntityUtil.sendUpdatePacket(this);
    }
    
    public Location getLinkLocation() {
        return this.linkLocation;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean b) {
        if (b && this.running != true) {
            this.ticker = 0;
            this.totalTicks = 0;
        }

        this.running = b;
        
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

    public boolean isValidLinked() {
        return this.getLinkLocation() != null && level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity;
    }

    public boolean isFirstIteration() {
        return this.totalTicks == this.ticker;
    }

    public void setPowered(boolean b) {
        this.powered = b;        
        BlockEntityUtil.sendUpdatePacket(this);
    }

    public boolean isPowered() {
        return this.powered;
    }
}
