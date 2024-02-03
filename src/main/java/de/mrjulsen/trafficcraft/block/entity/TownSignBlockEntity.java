package de.mrjulsen.trafficcraft.block.entity;

import java.util.Arrays;

import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.TownSignBlock.ETownSignSide;
import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.data.TownSignVariant;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class TownSignBlockEntity extends WritableTrafficSignBlockEntity {
    
    private String[] linesBack = null;

    protected TownSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TownSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

     @Override
    public de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig getRenderConfig() {
        float y = 120;
        float scaleA = 1;
        float scaleB = 2.5f;
        float scaleBLine = 3;
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(0, -1.0F / 16.0F * 6.0f, new Vec2(1, 1), new Vec2(scaleA, scaleA), 1.0F / 16.0F * 15, scaleA, 0),
            new ConfiguredLineData(0, -1.0F / 16.0F * 6.0f, new Vec2(1, 1), new Vec2(scaleB, scaleB), 1.0F / 16.0F * 14, scaleBLine, 0),
            new ConfiguredLineData(0, -1.0F / 16.0F * 6.0f, new Vec2(1, 1), new Vec2(scaleA, scaleA), 1.0F / 16.0F * 15, scaleA, 0),
            new ConfiguredLineData(0, -1.0F / 16.0F * 6.0f, new Vec2(1, 1), new Vec2(scaleA, scaleA), 1.0F / 16.0F * 15, scaleA, 0)
        }, false, 0, y, WritableSignConfig.DEFAULT_SCALE, 0, 0.0f, 0.0f, 0.1f, (blockState) -> {
            return blockState.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockState.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockState.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockState.getValue(WritableTrafficSign.FACING).toYRot(); 
        }, 0);
    }

    public WritableSignConfig getBackRenderConfig() {
        float y = 120;
        float scale = 2.5F;
        float lineScale = 3;
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(-1.0F / 16.0F * 1.5F, -1.0F / 16.0F * 4f, new Vec2(1, 1), new Vec2(scale, scale), 1.0F / 16.0F * 12, lineScale, 0),
            new ConfiguredLineData(0, -1.0F / 16.0F * 3.5f, new Vec2(1, 1), new Vec2(scale, scale), 1.0F / 16.0F * 14, lineScale, 0)
        }, false, 0, y, WritableSignConfig.DEFAULT_SCALE, 0, 0.0f, 0.0f, 0.1f, (blockState) -> {
            return blockState.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockState.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockState.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockState.getValue(WritableTrafficSign.FACING).toYRot() + (blockState.getValue(TownSignBlock.VARIANT) == TownSignVariant.BOTH ? 180 : 0); 
        }, 0);
    }
    
    public WritableSignConfig getTownSignRenderConfig(TownSignBlock.ETownSignSide side) {
        switch (side) {
            case BACK:
                return this.getBackRenderConfig();
            default:
            case FRONT:
                return this.getRenderConfig();
        }
    }


    private void initBackTextArray() {
        if (this.linesBack == null) {
            this.linesBack = new String[this.getTownSignRenderConfig(ETownSignSide.BACK).lineData().length];
            Arrays.fill(linesBack, "");
        }
    }

    public void setBackText(String text, int line) {
        if (line < 0 || line > this.getTownSignRenderConfig(ETownSignSide.BACK).lineData().length)
            return;

        initBackTextArray();

        this.linesBack[line] = text;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public void setBackTexts(String[] messages) {
        initBackTextArray();
        this.linesBack = messages;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    public String getBackText(int line) {
        initBackTextArray();        
        return this.linesBack == null ? null : this.linesBack[line];
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.linesBack = new String[this.getTownSignRenderConfig(ETownSignSide.BACK).lineData().length];
        for (int i = 0; i < this.getTownSignRenderConfig(ETownSignSide.BACK).lineData().length; i++) {
            this.linesBack[i] = compound.getString("lineBack" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.linesBack != null) {
            for (int i = 0; i < this.linesBack.length; i++) {
                tag.putString("lineBack" + i, this.linesBack[i]);
            }
        }
    }

}
