package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.data.WorldLocation;
import de.mrjulsen.trafficcraft.block.TrafficLightRequestButtonBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightRequestButtonBlockEntity extends DLSyncedBlockEntity {

    private static final String NBT_LISTENING = "listening";
    private static final String NBT_LINKED_TO = "linkedTo";

    // Properties
    private WorldLocation linkLocation;
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

        this.listening = compound.getBoolean(NBT_LISTENING);
        if (compound.contains(NBT_LINKED_TO)) {
            this.linkLocation = WorldLocation.loadFromNbt(compound.getCompound(NBT_LINKED_TO));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putBoolean(NBT_LISTENING, this.listening);
        if (this.linkLocation != null) {
            tag.put(NBT_LINKED_TO, linkLocation.toNbt());
        }
        super.saveAdditional(tag);
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
    public void linkTo(WorldLocation loc) {
        this.linkLocation = loc;
        notifyUpdate();
    }    

    public void clearLink() {
        this.linkLocation = null;
        notifyUpdate();
    }
    
    public WorldLocation getLinkLocation() {
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
