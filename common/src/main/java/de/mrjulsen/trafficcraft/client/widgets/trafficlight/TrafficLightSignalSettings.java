package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager.TrafficLightTextureKey;
import net.minecraft.ChatFormatting;

public class TrafficLightSignalSettings extends AbstractTrafficLightSettings {

    public static record SettingsResponse(TrafficLightColor color) {}

    private final OptionsPanel signalsPanel;
    
    private final TrafficLightConfig config;
    private final EventListenerId eventId;
    private final int signalIndex;

    public TrafficLightSignalSettings(int x, int y, int w, int h, int signalIndex, TrafficLightConfig config) {
        super(x, y, w, h, TextUtils.translate("gui.trafficcraft.trafficlight.set_signal"), true);
        this.signalIndex = signalIndex;
        this.config = config;
        
        eventId = config.addEventListener(TrafficLightConfig.UpdateEvent.class, (s, e) -> {
            refresh();
            return false;
        });

        int maxW = 18 * 9 + 2;
        this.signalsPanel = addComponent(new OptionsPanel(FlowLayout.Direction.HORIZONTAL));

        refresh();
    }

    private void refresh() {
        signalsPanel.clearComponents();
        TrafficLightColor[] colors = TrafficLightColor.getAllowedForType(config.type, true);
        for (TrafficLightColor color : colors) {
            final TrafficLightColor fColor = color;
            OptionButton btn = signalsPanel.addComponent(new OptionButton(new DLSprite(new DLTexture(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(config.icon, color)), 16, 16), 16, 16)));
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                btn.radioButtonMode.set(false);
                config.colors[signalIndex] = fColor;
                config.notifyUpdate();
                return false;
            });
            btn.tooltip.set(new DLTooltip(List.of(fColor.getValueTranslation().withStyle(ChatFormatting.BOLD), fColor.getValueDescriptionTranslation().withStyle(ChatFormatting.GRAY)), 200));
            btn.checked.set(config.colors[signalIndex] == fColor);
        }
    }
    
    @Override
    public void close() throws Exception {
        config.removeEventListener(TrafficLightConfig.UpdateEvent.class, eventId);
    }
}
