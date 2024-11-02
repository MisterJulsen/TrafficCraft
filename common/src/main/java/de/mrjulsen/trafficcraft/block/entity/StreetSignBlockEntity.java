package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.mcdragonlib.block.WritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class StreetSignBlockEntity extends WritableSignBlockEntity implements IColorBlockEntity {

    private PaintColor color = PaintColor.NONE;

    protected StreetSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StreetSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public WritableSignConfig getRenderConfig() {
        float y = 120;
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(0, -1.0F / 16.0F * 4.25f, new Vec2(1, 1.5f), new Vec2(1.5f, 1.5f), 1.0F / 16.0F * 15, 1, 0)
        }, true, 1.0F / 16.0F * 6.5f, y, WritableSignConfig.DEFAULT_SCALE, 90, 0.4f, 0.0f, 0.02f, (blockState) -> {
            return 90 + (blockState.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockState.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockState.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockState.getValue(WritableTrafficSign.FACING).toYRot()); 
        }, PaintColor.useWhiteOrBlackForeColor(this.getColor().getTextureColor()) ? DyeColor.WHITE.getTextColor() : DyeColor.BLACK.getTextColor());
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

    @Override
    public void setColor(PaintColor color) {
        this.color = color;
        notifyUpdate();
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 512);        
    }

    @Override
    public void setText(String text, int line) {
        super.setText(text, line);
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 512);
    }

    @Override
    public void setTexts(String[] messages) {
        super.setTexts(messages);
        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 512);
    }

    @Override
    public PaintColor getColor() {
        return this.color;
    }
}
