package de.mrjulsen.legacydragonlib.client.gui.widgets;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.widget.ForgeSlider;

@OnlyIn(Dist.CLIENT)
public class ModSlider extends ForgeSlider {

    private final Consumer<ModSlider> onUpdateMessage;

    public ModSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, Consumer<ModSlider> onUpdateMessage) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
        this.onUpdateMessage = onUpdateMessage;
    }

    public ModSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, Consumer<ModSlider> onUpdateMessage) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
        this.onUpdateMessage = onUpdateMessage;
        this.updateMessage();
    }
    
    @Override
    protected void updateMessage() {
        if (onUpdateMessage == null) {
            super.updateMessage();
            return;
        }
        
        onUpdateMessage.accept(this);
    }

    public void setPrefix(Component text) {
        this.prefix = text;
    }

    public void setSuffix(Component text) {
        this.suffix = text;
    }
    
}
