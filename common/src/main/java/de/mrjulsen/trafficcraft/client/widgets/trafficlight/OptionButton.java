package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.util.TextUtils;

public class OptionButton extends DLToggleButton {
    public OptionButton(DLSprite icon) {
        super(0, 0, 18, 18);
        componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        text.set(TextUtils.EMPTY);
        this.icon.set(icon);
        radioButtonMode.set(true);
    }    
}
