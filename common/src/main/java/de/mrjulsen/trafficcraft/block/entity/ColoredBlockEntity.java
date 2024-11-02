package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.mcdragonlib.block.SyncedBlockEntity;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredBlockEntity extends SyncedBlockEntity implements IColorBlockEntity {

    // Properties
    protected PaintColor color = PaintColor.NONE;

    protected ColoredBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ColoredBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLORED_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.color = PaintColor.getByIndex(tag.getInt(NBT_COLOR));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(NBT_COLOR, color.getIndex());
    }

    /* GETTERS AND SETTERS */
    @Override
    public void setColor(PaintColor color) {
        this.color = color;
        notifyUpdate();
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 512);
    }

    @Override
    public PaintColor getColor() {
        return this.color;
    }
}
