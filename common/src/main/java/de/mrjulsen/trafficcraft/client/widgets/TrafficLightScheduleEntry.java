package de.mrjulsen.trafficcraft.client.widgets;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent.ConsumptionType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.FlatButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.client.widgets.data.TrafficLightScheduleEditorWidget;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import net.minecraft.network.chat.Component;

public class TrafficLightScheduleEntry extends DLGuiComponent {

    private static final int CONTROL_BTN_SIZE = 16;

    private final DLPanel widgets;

    private final DLNumberPicker delaySelector;
    private final DLNumberPicker phaseIdBox;
    private final TrafficLightSignalButton[] signalButtons;

    private final TrafficLightColor[] signals;

    // texts
    private final Component textDelay = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delay");
    private final Component textPhaseId = TextUtils.translate("gui.trafficcraft.trafficlightschedule.phase_id");
    private final Component textMoveUp = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_up");
    private final Component textMoveDown = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_down");
    private final Component textDelete = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delete");

    // data
    private final boolean hidePhaseId;
    private final TrafficLightScheduleEntry instance = this;

    public TrafficLightScheduleEntry(int x, int y, int width, TrafficLightScheduleEntryData entry, boolean hidePhaseId, Map<Integer, TrafficLightType> signalTypes, Consumer<TrafficLightScheduleEntryData> removeAction, BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction) {
        super(x, y, width, TrafficLightScheduleEditorWidget.PADDING * 2 + 40);
        inputConsumptionPolicy.set(c -> c != ConsumptionType.SCROLL);

        this.hidePhaseId = hidePhaseId;

        delaySelector = addComponent(new DLNumberPicker(TrafficLightScheduleEditorWidget.TEXT_X, TrafficLightScheduleEditorWidget.PADDING / 2 + 2, 38, 16));
        delaySelector.max.set((double)TrafficLightScheduleEntryData.MAX_SECONDS);
        delaySelector.value.set(entry.getDurationSeconds());
        delaySelector.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
        delaySelector.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            entry.setDurationSeconds(MathUtils.clamp(e.value(), 0, TrafficLightScheduleEntryData.MAX_SECONDS));
            return false;
        });
        delaySelector.tooltip.set(new DLTooltip(List.of(textDelay), 200));

        int currentY = (int)(TrafficLightScheduleEditorWidget.PADDING * 1.5f + 20 + 2);
        int signalSelectionX = TrafficLightScheduleEditorWidget.TEXT_X;
        if (!hidePhaseId) {
            phaseIdBox = addComponent(new DLNumberPicker(signalSelectionX, currentY, 38, 16));
            phaseIdBox.showButtons.set(false);
            phaseIdBox.min.set(-9999D);
            phaseIdBox.max.set(9999D);
            phaseIdBox.value.set((double)entry.getPhaseId());
            phaseIdBox.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
            phaseIdBox.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
                entry.setPhaseId((int)e.value());
                return false;
            });
            phaseIdBox.tooltip.set(new DLTooltip(List.of(textPhaseId), 200));
            
            signalSelectionX += phaseIdBox.width() + 6;
        } else {
            phaseIdBox = null;
        }

        signals = TrafficLightColor.getAllowedForType(TrafficLightType.CAR, false);

        widgets = addComponent(new DLPanel(signalSelectionX, currentY, 10, 16));
        widgets.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.drawBox(e.graphics(), 0, 0, s.width(), s.height(), DLColor.BLACK, DLColor.fromInt(0xFFDBDBDB));
            }
            return false;
        });
        FlowLayout signalsLayout = new FlowLayout();
        signalsLayout.padding.set(new Padding(2, 3, 2, 3));
        signalsLayout.flowDirection.set(Direction.HORIZONTAL);
        signalsLayout.wrap.set(false);
        signalsLayout.horizontalGap.set(1);
        widgets.layout.set(signalsLayout);
        widgets.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
        widgets.addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            widgets.setWidth(e.layoutResult().contentWidth());
            return false;
        });
       
        this.signalButtons = new TrafficLightSignalButton[signals.length];
        for (int i = 0; i < signals.length; i++) {
            final int k = i;
            TrafficLightColor signal = signals[i];
            TrafficLightSignalButton signalBtn = this.signalButtons[k] = widgets.addComponent(new TrafficLightSignalButton(entry, signal, signalTypes.getOrDefault(entry.getPhaseId(), null)));
            signalBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
        }

        DLButton moveUpButton = addComponent(new DLButton(width() - CONTROL_BTN_SIZE - 4, 4, CONTROL_BTN_SIZE, CONTROL_BTN_SIZE));
        moveUpButton.anchor.set2(EAlign.TOP, EAlign.RIGHT);
        moveUpButton.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        moveUpButton.text.set(TextUtils.empty());
        moveUpButton.icon.set(ModGuiIcons.MOVE_UP.getAsSprite(16, 16));
        moveUpButton.inputConsumptionPolicy.set(p -> p != ConsumptionType.MOUSE_MOVE);
        moveUpButton.tooltip.set(new DLTooltip(List.of(textMoveUp), 200));
        moveUpButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            reorderAction.accept(entry, -1);
            return false;
        });

        DLButton moveDownButton = addComponent(new DLButton(width() - CONTROL_BTN_SIZE - 4, 4 + CONTROL_BTN_SIZE, CONTROL_BTN_SIZE, CONTROL_BTN_SIZE));
        moveDownButton.anchor.set2(EAlign.TOP, EAlign.RIGHT);
        moveDownButton.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        moveDownButton.text.set(TextUtils.empty());
        moveDownButton.icon.set(ModGuiIcons.MOVE_DOWN.getAsSprite(16, 16));
        moveDownButton.inputConsumptionPolicy.set(p -> p != ConsumptionType.MOUSE_MOVE);
        moveDownButton.tooltip.set(new DLTooltip(List.of(textMoveDown), 200));
        moveDownButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            reorderAction.accept(entry, 1);
            return false;
        });
        

        DLButton deleteBtn = addComponent(new DLButton(width() - CONTROL_BTN_SIZE - 4, height() - 4 - CONTROL_BTN_SIZE, CONTROL_BTN_SIZE, CONTROL_BTN_SIZE));
        deleteBtn.anchor.set2(EAlign.BOTTOM, EAlign.RIGHT);
        deleteBtn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        deleteBtn.text.set(TextUtils.empty());
        deleteBtn.icon.set(ModGuiIcons.DELETE_WHITE.getAsSprite(16, 16));
        deleteBtn.inputConsumptionPolicy.set(p -> p != ConsumptionType.MOUSE_MOVE);
        deleteBtn.tooltip.set(new DLTooltip(List.of(textDelete), 200));
        deleteBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            removeAction.accept(entry);
            return false;
        });
        
        /*
        setMenu(new DLContextMenu(() -> GuiAreaDefinition.of(this), () ->
            new DLContextMenuItem.Builder()
                .add(new ContextMenuItemData(textMoveUp, Sprite.empty(), true, (b) -> reorderAction.accept(entry, -1), null))
                .add(new ContextMenuItemData(textMoveDown, Sprite.empty(), true, (b) -> reorderAction.accept(entry, 1), null))
                .addSeparator()
                .add(new ContextMenuItemData(textDelete, Sprite.empty(), true, (b) -> removeAction.accept(entry), null))
        ));
        */
        
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            
        DLTextureSheet.DRAGONLIB_UI.getSprite("button_gray_normal")
            .render(
                graphics,
                TrafficLightScheduleEditorWidget.PADDING,
                TrafficLightScheduleEditorWidget.PADDING / 2,
                TrafficLightScheduleEditorWidget.ENTRY_PADDING_LEFT + TrafficLightScheduleEditorWidget.SPRITE_SIZE + TrafficLightScheduleEditorWidget.ENTRY_PADDING_TIMELINE_TEXT + delaySelector.width() + TrafficLightScheduleEditorWidget.ENTRY_PADDING_RIGHT,
                20
            );
            
        DLTextureSheet.DRAGONLIB_UI.getSprite("button_gray_normal")
            .render(
                graphics,
                TrafficLightScheduleEditorWidget.PADDING,
                (int)(TrafficLightScheduleEditorWidget.PADDING * 1.5f + 20),
                TrafficLightScheduleEditorWidget.ENTRY_PADDING_LEFT + TrafficLightScheduleEditorWidget.SPRITE_SIZE + TrafficLightScheduleEditorWidget.ENTRY_PADDING_TIMELINE_TEXT + (phaseIdBox == null ? 0 : phaseIdBox.width() + 6) + widgets.width() + TrafficLightScheduleEditorWidget.ENTRY_PADDING_RIGHT,
                20
            );



        TrafficLightScheduleEditorWidget.ICONS.getSprite(TrafficLightScheduleEditorWidget.SPRITE_TIMELINE_EDGE)
            .render(
                graphics,
                TrafficLightScheduleEditorWidget.TIMELINE_ICON_X,
                0,
                TrafficLightScheduleEditorWidget.SPRITE_SIZE,
                height()
            );

        TrafficLightScheduleEditorWidget.ICONS.getSprite(TrafficLightScheduleEditorWidget.SPRITE_TIMELINE_DELAY)
            .render(
                graphics,
                TrafficLightScheduleEditorWidget.TIMELINE_ICON_X,
                TrafficLightScheduleEditorWidget.PADDING + 10 / TrafficLightScheduleEditorWidget.SPRITE_SIZE / 2,
                TrafficLightScheduleEditorWidget.SPRITE_SIZE,
                TrafficLightScheduleEditorWidget.SPRITE_SIZE
            );
            
        TrafficLightScheduleEditorWidget.ICONS.getSprite(TrafficLightScheduleEditorWidget.SPRITE_TIMELINE_ACTION)
            .render(
                graphics,
                TrafficLightScheduleEditorWidget.TIMELINE_ICON_X,
                (int)(TrafficLightScheduleEditorWidget.PADDING * 1.5f + 30 - TrafficLightScheduleEditorWidget.SPRITE_SIZE / 2),
                TrafficLightScheduleEditorWidget.SPRITE_SIZE,
                TrafficLightScheduleEditorWidget.SPRITE_SIZE
            );
            
        if (isSelected()) {            
            GuiUtils.fill(graphics, 0, 0, width(), height(), DLColor.fromInt(0x22FFFFFF));
        }
    }
}
