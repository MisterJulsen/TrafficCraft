package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;

import org.joml.Vector2f;

import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StreetSignBlockEntity extends WritableTrafficSignBlockEntity implements IColorBlockEntity {
    
    private PaintColor color = PaintColor.NONE;

    protected StreetSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StreetSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public WritableSignConfig getRenderConfig() {
        int y = 120 + (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 5));
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(0, y + (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * (-5.0f + 0.75f))), new Vector2f(1, 1.5f), new Vector2f(1.5f, 1.5f), (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 15)), 10, 0)
        }, (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 22.5f)), y, WritableSignConfig.DEFAULT_SCALE, 0, 90, 0);
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
