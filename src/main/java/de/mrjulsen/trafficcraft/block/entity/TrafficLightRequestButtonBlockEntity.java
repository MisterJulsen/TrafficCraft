package de.mrjulsen.trafficcraft.block.entity;

import javax.annotation.Nullable;

import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightRequestButtonBlockEntity extends BlockEntity {

    // Properties
    private Location linkLocation;
    private boolean listening;

    protected TrafficLightRequestButtonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrafficLightRequestButtonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_LIGHT_REQUEST_BUTTON_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        this.listening = compound.getBoolean("listening");
        if (compound.contains("linkedTo")) {
            this.linkLocation = Location.fromNbt(compound.getCompound("linkedTo"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putBoolean("listening", this.listening);
        if (this.linkLocation != null) {
            tag.put("linkedTo", linkLocation.toNbt());
        }
        super.saveAdditional(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
        if (this.listening) {
            if (!level.isClientSide) {
                boolean isRunning = false;

                if (this.linkLocation != null) {
                    if (level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightBlockEntity blockEntity) {
                        isRunning = blockEntity.isFirstIteration();
                    } else  if (level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
                        isRunning = blockEntity.isFirstIteration();
                    }
                }
                
                if (!isRunning) {
                    this.listening = false;
                    level.setBlockAndUpdate(pos, state.setValue(TrafficLightRequestButtonBlock.ACTIVATED, false).setValue(TrafficLightRequestButtonBlock.POWERED, false));                    
                }
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TrafficLightRequestButtonBlockEntity blockEntity) {
        blockEntity.tick(level, pos, state);
    }


    /* GETTERS AND SETTERS */
    public void linkTo(Location loc) {
        this.linkLocation = loc;
        setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }    

    public void clearLink() {
        this.linkLocation = null;
        BlockEntityUtil.sendUpdatePacket(this);
    }
    
    public Location getLinkLocation() {
        return this.linkLocation;
    }

    public boolean isValidLinked() {
        return this.getLinkLocation() != null && (
            level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity ||
            level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightBlockEntity
        );
    }

    public boolean isListening() {
        return this.listening;
    }

    public boolean activate() {
        if (!this.isValidLinked()) 
            return false;
            
        this.listening = true;

        if (level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightBlockEntity blockEntity) {
            if (blockEntity.getSchedule().getTrigger() == TrafficLightTrigger.ON_REQUEST) {
                blockEntity.startSchedule(true);
                return true;
            }
        } else if (level.getBlockEntity(this.linkLocation.getLocationBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
            if (blockEntity.getFirstOrMainSchedule().getTrigger() == TrafficLightTrigger.ON_REQUEST) {
                blockEntity.startSchedule(true);
                return true;
            }
        }
        
        this.listening = false;

        return false;
    }
}
