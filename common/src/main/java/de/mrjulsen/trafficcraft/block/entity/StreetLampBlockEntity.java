package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.util.time.ConfiguredTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StreetLampBlockEntity extends DLSyncedBlockEntity {

    private static final String NBT_TURN_ON_TIME = "turnOnTime";
    private static final String NBT_TURN_OFF_TIME = "turnOffTime";

    // Properties
    private int onTimeTicks = 0;
    private int offTimeTicks = 0;

    protected StreetLampBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StreetLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STREET_LAMP_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        this.onTimeTicks = compound.getInt(NBT_TURN_ON_TIME);
        this.offTimeTicks = compound.getInt(NBT_TURN_OFF_TIME);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt(NBT_TURN_ON_TIME, onTimeTicks);
        tag.putInt(NBT_TURN_OFF_TIME, offTimeTicks);
        super.saveAdditional(tag);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (this.getOffTime() == this.getOnTime()) {
            return;
        }

        ConfiguredTimeSystem system = new ConfiguredTimeSystem();
        long time = (long)DLTime.fromLevelTime(level, system).getTicks() % system.getTicksPerDay();

        if (isInRange(time, onTimeTicks, offTimeTicks)) {
            if (!state.getValue(StreetLampBaseBlock.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(StreetLampBaseBlock.LIT, true));
            }
        } else {
            if (state.getValue(StreetLampBaseBlock.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(StreetLampBaseBlock.LIT, false));
            }
        }
    }

    public static boolean isInRange(long time, long start, long end) {
        time %= 24000;
        start %= 24000;
        end %= 24000;
        if (start <= end) {
            return time >= start && time <= end;
        } else {
            return time >= start || time <= end;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StreetLampBlockEntity blockEntity) {
        blockEntity.tick(level, pos, state);
    }

    public int getOnTime() {
        return this.onTimeTicks;
    }

    public int getOffTime() {
        return this.offTimeTicks;
    }

    public void setOnTime(int time) {
        this.onTimeTicks = Mth.clamp(time, 0, 23999);
        notifyUpdate();
    }

    public void setOffTime(int time) {
        this.offTimeTicks = Mth.clamp(time, 0, 23999);
        notifyUpdate();
    }
}
