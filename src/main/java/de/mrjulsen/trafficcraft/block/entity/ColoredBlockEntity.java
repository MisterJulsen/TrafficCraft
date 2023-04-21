package de.mrjulsen.trafficcraft.block.entity;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredBlockEntity extends BlockEntity {

    // Properties
    private PaintColor color = PaintColor.NONE;

    protected ColoredBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ColoredBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLORED_BLOCK_ENTITY.get(), pos, state);
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
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 11, 0);
    }

    /* GETTERS AND SETTERS */
    public void setColor(PaintColor color) {
        this.color = color;
        BlockEntityUtil.sendUpdatePacket(this);
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 11, 0);
    }

    public PaintColor getColor() {
        return this.color;
    }
}
