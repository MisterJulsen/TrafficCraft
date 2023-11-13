package de.mrjulsen.trafficcraft.block.entity;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.data.Location;
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
                if (level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightBlockEntity blockEntity) {
                    isRunning = blockEntity.isFirstIteration();
                } else  if (level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
                    isRunning = blockEntity.isFirstIteration();
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

    public boolean isValidLinked() {
        return this.getLinkLocation() != null && level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity;
    }

    public boolean isListening() {
        return this.listening;
    }

    public boolean activate() {
        this.listening = true;
        if (!this.isValidLinked()) 
            return false;

        if (level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightBlockEntity blockEntity) {
            if (blockEntity.getSchedule().getTrigger() == TrafficLightTrigger.ON_REQUEST) {
                blockEntity.startSchedule(false);
                return true;
            }
        } else  if (level.getBlockEntity(this.linkLocation.getLocationAsBlockPos()) instanceof TrafficLightControllerBlockEntity blockEntity) {
            if (blockEntity.getFirstOrMainSchedule().getTrigger() == TrafficLightTrigger.ON_REQUEST) {
                blockEntity.startSchedule(false);
                return true;
            }
        }

        return false;
    }
}
