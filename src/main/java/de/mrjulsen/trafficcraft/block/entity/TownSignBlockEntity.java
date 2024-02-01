package de.mrjulsen.trafficcraft.block.entity;

import java.util.Arrays;

import org.joml.Vector2f;

import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.TownSignBlock.ETownSignSide;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
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

    public SignRenderingConfig getRenderingConfigold() {
        SignRenderingConfig config = new SignRenderingConfig(4);
        config.lineHeightMultiplier[0] = 1.8D;
        config.lineHeightMultiplier[1] = 2.5D;
        config.textYOffset = 75;
        config.setFontScale(1, new SignRenderingConfig.AutomaticFontScaleConfig(1.0D, 2.0D));
        return config;
    }

    @Override
    public WritableSignConfig getRenderConfig() {
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(0, (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 0.5f)), new Vector2f(1, 1), new Vector2f(1, 1), (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 8)), 10, 0)
        }, 0, 120, WritableSignConfig.DEFAULT_SCALE, 0, 180, 0);
    }

    public WritableSignConfig getBackRenderConfig() {
        return new WritableSignConfig(new ConfiguredLineData[] {
            new ConfiguredLineData(0, (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 0.5f)), new Vector2f(1, 1), new Vector2f(1, 1), (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 8)), 10, 0)
        }, 0, 120, WritableSignConfig.DEFAULT_SCALE, 0, 180, 0);
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
