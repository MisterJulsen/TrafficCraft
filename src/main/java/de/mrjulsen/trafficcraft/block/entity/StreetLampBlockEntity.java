package de.mrjulsen.trafficcraft.block.entity;

import javax.annotation.Nullable;

import de.mrjulsen.mcdragonlib.utils.TimeUtils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.StreetLampBaseBlock;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StreetLampBlockEntity extends BlockEntity {

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
    public void load(CompoundTag compound)
    {
        super.load(compound);

        this.onTimeTicks = compound.getInt("turnOnTime");
        this.offTimeTicks = compound.getInt("turnOffTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        tag.putInt("turnOnTime", onTimeTicks);
        tag.putInt("turnOffTime", offTimeTicks);
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

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (this.getOffTime() == this.getOnTime()) {
            return;
        }

        if (TimeUtils.isInRange((int)(level.getDayTime() % Constants.TICKS_PER_DAY), onTimeTicks, offTimeTicks)) {
            if (!state.getValue(StreetLampBaseBlock.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(StreetLampBaseBlock.LIT, true));
            }
        } else {
            if (state.getValue(StreetLampBaseBlock.LIT)) {
                level.setBlockAndUpdate(pos, state.setValue(StreetLampBaseBlock.LIT, false));
            }
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
        this.onTimeTicks = Mth.clamp(time, 0, Constants.TICKS_PER_DAY - 1);
    }

    public void setOffTime(int time) {
        this.offTimeTicks = Mth.clamp(time, 0, Constants.TICKS_PER_DAY - 1);
    }
}
