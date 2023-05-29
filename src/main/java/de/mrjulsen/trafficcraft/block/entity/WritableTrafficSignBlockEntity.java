package de.mrjulsen.trafficcraft.block.entity;

import java.util.Arrays;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.client.SignRenderingConfig;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WritableTrafficSignBlockEntity extends BlockEntity {
    private String[] lines = null;

    protected WritableTrafficSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public WritableTrafficSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    public abstract SignRenderingConfig getRenderingConfig();

    private void initTextArray() {
        if (this.lines == null) {
            this.lines = new String[this.getRenderingConfig().getLines()];
            Arrays.fill(lines, "");
        }
    }

    public void setText(String text, int line) {
        if (line < 0 || line > this.getRenderingConfig().getLines())
            return;

        initTextArray();

        this.lines[line] = text;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public void setTexts(String[] messages) {
        initTextArray();
        this.lines = messages;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public String getText(int line) {
        initTextArray();        
        return this.lines == null ? null : this.lines[line];
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.lines = new String[this.getRenderingConfig().getLines()];
        for (int i = 0; i < this.getRenderingConfig().getLines(); i++) {
            this.lines[i] = compound.getString("line" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.lines != null) {
            for (int i = 0; i < this.getRenderingConfig().getLines(); i++) {
                tag.putString("line" + i, this.lines[i]);
            }
        }
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
        this.level.markAndNotifyBlock(this.worldPosition, this.level.getChunkAt(this.worldPosition), this.getBlockState(), this.getBlockState(), 3, 512);
    }
}
