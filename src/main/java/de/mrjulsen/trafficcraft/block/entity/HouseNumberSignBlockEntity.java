package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.block.data.IColorBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig;
import de.mrjulsen.trafficcraft.data.PaintColor;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HouseNumberSignBlockEntity extends WritableTrafficSignBlockEntity implements IColorBlockEntity {
    
    private PaintColor color = PaintColor.NONE;

    protected HouseNumberSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public HouseNumberSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public SignRenderingConfig getRenderingConfig() {
        SignRenderingConfig config = new SignRenderingConfig(1);
        //config.textureYOffset = config.height() / 2;
        config.maxLineWidth = config.width() / 2;
        config.textureYOffset = 40;
        config.setFontScale(0, new SignRenderingConfig.AutomaticFontScaleConfig(1.0D, 3.0D));
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
