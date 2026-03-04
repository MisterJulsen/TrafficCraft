package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import net.minecraft.ChatFormatting;

public class TrafficLightGeneralSettings extends AbstractTrafficLightSettings {

    public static record SettingsResponse(TrafficLightType type, TrafficLightModel model, TrafficLightIcon icon) {}

    private final OptionsPanel typePanel;
    private final OptionsPanel signalsCountPanel;
    private final OptionsPanel iconPanel;

    private final TrafficLightConfig config;
    private final EventListenerId eventId;

    public TrafficLightGeneralSettings(int x, int y, int w, int h, TrafficLightConfig config) {
        super(x, y, w, h, TextUtils.translate("gui.trafficcraft.trafficlight.general_settings"), true);
        this.config = config;

        eventId = config.addEventListener(TrafficLightConfig.UpdateEvent.class, (s, e) -> {
            reloadSignals();
            reloadIcons();
            return false;
        });

        this.typePanel = addComponent(new OptionsPanel(FlowLayout.Direction.HORIZONTAL));
        this.signalsCountPanel = addComponent(new OptionsPanel(FlowLayout.Direction.HORIZONTAL));
        this.iconPanel = addComponent(new OptionsPanel(FlowLayout.Direction.HORIZONTAL));

        for (TrafficLightType type : TrafficLightType.values()) {
            final TrafficLightType fType = type;
            DLToggleButton typeBtn = typePanel.addComponent(new DLToggleButton(0, 0, 150, 18));
            typeBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
            typeBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            typeBtn.text.set(type.getValueTranslation());
            typeBtn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
            typeBtn.drawFontShadow.set(false);
            typeBtn.icon.set(type.getSprite()); 
            typeBtn.iconAlignment.set(ETextAlignment.LEFT);
            typeBtn.textAlignment.set(ETextAlignment.LEFT); 
            typeBtn.radioButtonMode.set(true);
            typeBtn.checked.set(fType == config.type);
            typeBtn.tooltip.set(new DLTooltip(List.of(fType.getValueTranslation().withStyle(ChatFormatting.BOLD), fType.getValueDescriptionTranslation().withStyle(ChatFormatting.GRAY)), 200));
            typeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                config.type = fType;
                config.notifyUpdate();
                return false;
            });
        }

        reloadSignals();
        reloadIcons();
    }
    
    @Override
    public void close() throws Exception {
        config.removeEventListener(TrafficLightConfig.UpdateEvent.class, eventId);
    }

    private void reloadSignals() {
        signalsCountPanel.clearComponents();
        for (TrafficLightModel model : TrafficLightModel.values()) {
            final TrafficLightModel fModel = model;
            OptionButton btn = new OptionButton(model.getIcon());
            if (fModel == config.model) btn.checked.set(true);
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                btn.radioButtonMode.set(false);
                config.model = fModel;
                config.notifyUpdate();
                return false;
            });
            btn.tooltip.set(new DLTooltip(List.of(fModel.getValueTranslation()), 200));
            signalsCountPanel.addComponent(btn);
        }
    }

    private void reloadIcons() {
        iconPanel.clearComponents();
        TrafficLightIcon[] icons = TrafficLightIcon.getAllowedForType(config.type);
        for (TrafficLightIcon icon : icons) {
            final TrafficLightIcon fIcon = icon;
            OptionButton btn = new OptionButton(icon.getSprite(config.type));
            btn.checked.set(fIcon == config.icon);
            if (config.type == TrafficLightType.TRAM) {
                btn.tooltip.set(new DLTooltip(List.of(fIcon.getValueTranslation().withStyle(ChatFormatting.BOLD), fIcon.getValueDescriptionTranslation().withStyle(ChatFormatting.GRAY)), 200));
            } else {
                btn.tooltip.set(new DLTooltip(List.of(fIcon.getValueTranslation()), 200));
            }
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                btn.radioButtonMode.set(false);
                config.icon = fIcon;
                config.notifyUpdate();
                return false;
            });
            iconPanel.addComponent(btn);
        }
    }
}
