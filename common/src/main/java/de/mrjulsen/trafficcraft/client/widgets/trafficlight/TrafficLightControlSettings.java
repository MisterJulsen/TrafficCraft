package de.mrjulsen.trafficcraft.client.widgets.trafficlight;

import java.util.List;
import java.util.Optional;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.Clipboard;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager.TrafficLightTextureKey;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class TrafficLightControlSettings extends DLGuiComponent {


    public static final DLTextureSheet ICONS = new DLTextureSheet(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_light_schedule_icons.png"));
    private static final float SMALL_SCALE_VALUE = 0.75f;

    protected final Component title = TrafficLightControlType.STATIC.getEnumTranslation();
    private final Component textSetEnabledColors = TextUtils.translate("gui.trafficcraft.trafficlight.set_enabled_colors");
    private final Component textStatus = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.status");
    private final Component textEditSchedule = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.edit_schedule");
    private final Component textSetPhaseId = TextUtils.translate("gui.trafficcraft.trafficlight.set_phase_id");
    private final Component textPhaseIdDescription = TextUtils.translate("gui.trafficcraft.trafficlight.set_phase_id.description");

    
    private MultiLineLabel phaseIdDescriptionLabel;

    private final TrafficLightConfig config;
    private final EventListenerId eventId;

    private final DLPanel controlTypePanel;
    private final DLPanel settingsPanel;

    public TrafficLightControlSettings(int x, int y, int w, int h, TrafficLightConfig config) {
        super(x, y, w, h);
        this.config = config;

        eventId = config.addEventListener(TrafficLightConfig.UpdateEvent.class, (s, e) -> {
            refreshSettingsPanel();
            return false;
        });
        
        FlowLayout layout = new FlowLayout();
        layout.padding.set(new Padding(20, 7, 7, 7));
        layout.flowDirection.set(Direction.VERTICAL);
        layout.fillCrossAxis.set(true);
        layout.wrap.set(false);
        this.layout.set(layout);

        this.controlTypePanel = addComponent(new DLPanel(0, 0, width(), 20));
        FlowLayout controlTypeLayout = new FlowLayout();
        controlTypeLayout.padding.set(new Padding(1));
        controlTypeLayout.flowDirection.set(Direction.HORIZONTAL);
        controlTypeLayout.wrap.set(false);
        controlTypePanel.layout.set(controlTypeLayout);
        controlTypePanel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {                
                DLTextureSheet.DRAGONLIB_UI.getSprite("slot").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });

        this.settingsPanel = addComponent(new DLPanel(0, 0, width(), 20));
        settingsPanel.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        FlowLayout settingsLayout = new FlowLayout();
        settingsLayout.padding.set(new Padding(1));
        settingsLayout.flowDirection.set(Direction.VERTICAL);
        settingsLayout.wrap.set(false);
        settingsLayout.fillCrossAxis.set(true);
        settingsPanel.layout.set(settingsLayout);
        settingsPanel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {                
                ICONS.getSprite("container").render(e.graphics(), 0, 0, s.width(), s.height());
            }
            return false;
        });



        DLCycleButton<TrafficLightControlType> triggerBtn = controlTypePanel.addComponent(new DLCycleButton<>(0, 0, 1, 18));
        triggerBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        triggerBtn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        triggerBtn.drawFontShadow.set(false);
        triggerBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        triggerBtn.textFormat.set(c -> c.selectedItem.get().map(e -> e.getValueTranslation()).orElse(TextUtils.empty()));
        triggerBtn.icon.set(new DLSprite(config.controlType.getIconStack(), 16, false));
        triggerBtn.iconAlignment.set(ETextAlignment.LEFT);
        triggerBtn.textAlignment.set(ETextAlignment.LEFT);
        triggerBtn.items.addAll(TrafficLightControlType.values());
        triggerBtn.selectedItem.set(Optional.of(config.controlType));
        triggerBtn.tooltip.set(new DLTooltip(GuiUtils.getEnumTooltipData(TrafficLightControlType.class, 200), 200));
        triggerBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            triggerBtn.selectedItem.get().ifPresent(c -> {
                config.controlType = c;
                triggerBtn.icon.set(new DLSprite(c.getIconStack(), 16, false));
                refreshSettingsPanel();
                config.notifyUpdate();
            });
            return false;
        });

        addEventListener(DLGuiStandardEvents.ScreenLayoutUpdatedEvent.class, (s, e) -> {
            refreshSettingsPanel();
            return false;
        });
    }

    @Override
    public void close() throws Exception {
        config.removeEventListener(TrafficLightConfig.UpdateEvent.class, eventId);
    }

    private void refreshSettingsPanel() {
        settingsPanel.clearComponents();
        switch (config.controlType) {
            case OWN_SCHEDULE -> initScheduleSettings();
            case REMOTE -> initRemoteSettings();
            default -> initStaticSettings();
        }
    }

    private void initStaticSettings() {
        DLPanel panel = settingsPanel.addComponent(new DLPanel(0, 0, 1, 18));
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(false);
        panel.layout.set(layout);        

        TrafficLightColor[] colors = TrafficLightColor.getAllowedForType(config.type, false);
        for (TrafficLightColor color : colors) {
            final TrafficLightColor fColor = color;
            OptionButton btn = new OptionButton(new DLSprite(new DLTexture(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(config.icon, color)), 16, 16), 16, 16));
            btn.radioButtonMode.set(false);
            btn.layoutContraint.set(FlowLayout.FlowConstraint.END);
            btn.tooltip.set(new DLTooltip(List.of(fColor.getValueTranslation().withStyle(ChatFormatting.BOLD), fColor.getValueDescriptionTranslation().withStyle(ChatFormatting.GRAY)), 200));
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                btn.radioButtonMode.set(false);
                if (config.enabledColors.contains(fColor)) {
                    config.enabledColors.remove(fColor);
                } else {
                    config.enabledColors.add(fColor);
                }
                config.notifyUpdate();
                return false;
            });
            btn.checked.set(config.enabledColors.contains(fColor));
            panel.addComponent(btn);
        }

        panel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), 5, s.height() / 2 - e.graphics().defaultFont().lineHeight / 2, textSetEnabledColors, DLColor.fromInt(0xFFDBDBDB), ETextAlignment.LEFT, false);
            }
            return false;
        });
    }

    private void initScheduleSettings() {
        DLPanel schedulePanel = new DLPanel(0, 0, 0, 18);
        FlowLayout statusLayout = new FlowLayout();
        statusLayout.flowDirection.set(Direction.HORIZONTAL);
        statusLayout.wrap.set(false);
        schedulePanel.layout.set(statusLayout);

        DLButton editScheduleButton = schedulePanel.addComponent(new DLButton(0, 0, 0, 18));        
        editScheduleButton.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        editScheduleButton.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        editScheduleButton.text.set(textEditSchedule);
        editScheduleButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new TrafficLightScheduleEditor(mgr, config.level, config.blockPos));
            return false;
        });
        
        // copy
        DLButton copyBtn = schedulePanel.addComponent(new DLButton(0, 0, 18, 18));
        copyBtn.text.set(TextUtils.EMPTY);
        copyBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        copyBtn.icon.set(ModGuiIcons.COPY.getAsSprite(16, 16));
        copyBtn.layoutContraint.set(FlowLayout.FlowConstraint.END);
        copyBtn.tooltip.set(new DLTooltip(List.of(Constants.textCopy), 200));
        copyBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (config.level.getBlockEntity(config.blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                Clipboard.put(TrafficLightSchedule.class, blockEntity.getFirstOrMainSchedule());
            }
            return false;
        });
        
        DLButton pasteButton = schedulePanel.addComponent(new DLButton(0, 0, 18, 18));
        pasteButton.text.set(TextUtils.EMPTY);
        pasteButton.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        pasteButton.icon.set(ModGuiIcons.PASTE.getAsSprite(16, 16));
        pasteButton.layoutContraint.set(FlowLayout.FlowConstraint.END);
        pasteButton.tooltip.set(new DLTooltip(List.of(Constants.textPaste), 200));
        pasteButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            Optional<TrafficLightSchedule> schedule = Clipboard.get(TrafficLightSchedule.class);
            if (schedule.isPresent()) {
                ModNetworkManager.UPDATE_TRAFFIC_LIGHT_SCHEDULE.send(NetworkDirection.toServer(), new TrafficLightSchedulePacket(config.blockPos, List.of(schedule.get())));
            }
            return false;
        });
        

        DLCycleButton<Boolean> statusButton = new DLCycleButton<>(0, 0, 0, 18);
        statusButton.text.set(textStatus);
        statusButton.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_GRAY);
        statusButton.selectedItem.set(Optional.of(config.scheduleEnabled));
        statusButton.cycling.set(true);
        statusButton.items.addAll(true, false);
        statusButton.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(c.selectedItem.get().map(b -> b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).orElse(CommonComponents.OPTION_OFF)));
        statusButton.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {            
            statusButton.selectedItem.get().ifPresent(v -> config.scheduleEnabled = v);
            config.notifyUpdate();
            return false;
        });

        settingsPanel.addComponent(schedulePanel);
        settingsPanel.addComponent(statusButton);
    }

    private void initRemoteSettings() {
        DLPanel panel = new DLPanel(0, 0, 1, 18);
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(false);

        DLNumberPicker idPicker = new DLNumberPicker(0, 0, 50, 18);
        idPicker.min.set(-9999D);
        idPicker.max.set(9999D);
        idPicker.showButtons.set(false);
        idPicker.layoutContraint.set(FlowLayout.FlowConstraint.END);
        idPicker.value.set((double)config.phaseId);
        idPicker.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            config.phaseId = (int)e.value();
            config.notifyUpdate();
            return false;
        });
        panel.addComponent(idPicker);

        panel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), 5, s.height() / 2 - e.graphics().defaultFont().lineHeight / 2, textSetPhaseId, DLColor.fromInt(0xFFDBDBDB), ETextAlignment.LEFT, false);
            }
            return false;
        });

        DLPanel descriptionPanel = new DLPanel(0, 0, 0, 0);
        descriptionPanel.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        descriptionPanel.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                e.graphics().poseStack().pushPose();
                e.graphics().poseStack().scale(SMALL_SCALE_VALUE, SMALL_SCALE_VALUE, SMALL_SCALE_VALUE);
                phaseIdDescriptionLabel.renderLeftAlignedNoShadow(e.graphics().graphics(), (int)(5 / SMALL_SCALE_VALUE), (int)(5 / SMALL_SCALE_VALUE), 10, 0xFFDBDBDB);
                e.graphics().poseStack().popPose();
            }
            return false;
        });

        panel.layout.set(layout);
        settingsPanel.addComponent(panel);
        settingsPanel.addComponent(descriptionPanel);

        phaseIdDescriptionLabel = MultiLineLabel.create(Minecraft.getInstance().font, (int)((panel.width() - 10) / SMALL_SCALE_VALUE), 10, textPhaseIdDescription);
    }

    

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 7, 7, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
    
}
