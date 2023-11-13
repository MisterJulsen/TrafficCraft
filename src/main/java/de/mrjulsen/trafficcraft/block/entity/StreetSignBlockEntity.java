package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
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
    public SignRenderingConfig getRenderingConfig() {
        SignRenderingConfig config = new SignRenderingConfig(1);
        config.modelRotation = 90;
        config.textureYOffset = 72;
        config.textureXOffset = config.width() / 2 - (config.width() / 32 * 3);
        return config;
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
