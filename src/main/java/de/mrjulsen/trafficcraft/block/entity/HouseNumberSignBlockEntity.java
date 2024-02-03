package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class HouseNumberSignBlockEntity extends WritableTrafficSignBlockEntity implements IColorBlockEntity {
    
    private PaintColor color = PaintColor.NONE;

    protected HouseNumberSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public HouseNumberSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public WritableSignConfig getRenderConfig() {
            int y = 120;
            int maxScale = 3;
            return new WritableSignConfig(new ConfiguredLineData[] {
                new ConfiguredLineData(0, 1.0F / 16.0F * 0.5F, new Vec2(1, 1), new Vec2(maxScale, maxScale), 1.0F / 16.0F * 8, maxScale, 0)
            }, false, 0, y, WritableSignConfig.DEFAULT_SCALE, 0, 0.0f, 0.0f, -0.45f, (blockState) -> {
                return blockState.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockState.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockState.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockState.getValue(WritableTrafficSign.FACING).toYRot(); 
            }, PaintColor.useWhiteOrBlackForeColor(this.getColor().getTextureColor()) ? DyeColor.WHITE.getTextColor() : DyeColor.BLACK.getTextColor());
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        this.color = PaintColor.byId(compound.getInt("color"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        tag.putInt("color", color.getId());
        super.saveAdditional(tag);
    }

    @Override
    public void setColor(PaintColor color) {
        this.color = color;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    @Override
    public PaintColor getColor() {
        return this.color;
    }
}
