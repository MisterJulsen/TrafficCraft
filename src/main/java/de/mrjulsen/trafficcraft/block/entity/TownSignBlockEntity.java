package de.mrjulsen.trafficcraft.block.entity;

import java.util.Arrays;

import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.TownSignBlock.ETownSignSide;
import de.mrjulsen.trafficcraft.block.client.SignRenderingConfig;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TownSignBlockEntity extends WritableTrafficSignBlockEntity {
    
    private String[] linesBack = null;

    protected TownSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TownSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public SignRenderingConfig getRenderingConfig() {
        SignRenderingConfig config = new SignRenderingConfig(4);
        config.lineHeightMultiplier[0] = 1.8D;
        config.lineHeightMultiplier[1] = 2.5D;
        config.textYOffset = 75;
        config.setFontScale(1, new SignRenderingConfig.AutomaticFontScaleConfig(1.0D, 2.0D));
        return config;
    }

    private SignRenderingConfig getBackRenderingConfig() {
        SignRenderingConfig config = new SignRenderingConfig(2);
        config.lineHeightMultiplier[0] = 4.0D;
        config.lineHeightMultiplier[1] = 4.0D;
        config.textYOffset = 79;
        config.setFontScale(0, new SignRenderingConfig.AutomaticFontScaleConfig(1.0D, 2.0D));
        config.setFontScale(1, new SignRenderingConfig.AutomaticFontScaleConfig(1.0D, 2.0D));
        return config;
    }

    public SignRenderingConfig getTownSignRenderConfig(TownSignBlock.ETownSignSide side) {
        switch (side) {
            case BACK:
                return this.getBackRenderingConfig();
            default:
            case FRONT:
                return this.getRenderingConfig();
        }
    }


    private void initBackTextArray() {
        if (this.linesBack == null) {
            this.linesBack = new String[this.getTownSignRenderConfig(ETownSignSide.BACK).getLines()];
            Arrays.fill(linesBack, "");
        }
    }

    public void setBackText(String text, int line) {
        if (line < 0 || line > this.getTownSignRenderConfig(ETownSignSide.BACK).getLines())
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
        this.linesBack = new String[this.getTownSignRenderConfig(ETownSignSide.BACK).getLines()];
        for (int i = 0; i < this.getTownSignRenderConfig(ETownSignSide.BACK).getLines(); i++) {
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
