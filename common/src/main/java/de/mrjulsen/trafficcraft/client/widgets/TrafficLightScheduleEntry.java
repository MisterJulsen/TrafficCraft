package de.mrjulsen.trafficcraft.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.WidgetContainer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.DLWidgetsCollection;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TrafficLightScheduleEntry extends WidgetContainer {

    private static final int LEFT_PADDING = TrafficLightScheduleContainer.LEFT_PADDING;
    private static final int TIMELINE_WIDTH = TrafficLightScheduleContainer.TIMELINE_WIDTH;
    private static final int SPACING = TrafficLightScheduleContainer.SPACING;

    private static final ResourceLocation ICONS = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_light_schedule_icons.png");
    private static final Sprite MOVE_UP_ICON = new Sprite(ICONS, 64, 64, 24, 29, 12, 12);
    private static final Sprite MOVE_DOWN_ICON = new Sprite(ICONS, 64, 64, 12, 29, 12, 12);
    private static final Sprite DELETE_ICON = new Sprite(ICONS, 64, 64, 0, 29, 12, 12);

    private static final int DEFAULT_EDIT_BOX_HEIGHT = 18;
    private static final int DEFAULT_ENTRY_HEIGHT = 22;
    private static final int CONTROL_BUTTON_SIZE = 16;
    private static final int SIGNAL_ICON_SIZE = 10;
    public static final int HEIGHT = TrafficLightScheduleEditor.ENTRY_PADDING * 2 + DEFAULT_ENTRY_HEIGHT * 2;

    private final DLWidgetsCollection widgets = new DLWidgetsCollection();
    private final TrafficLightScheduleContainer parent;

    private final DLNumberSelector delaySelector;
    private final DLNumberSelector phaseIdBox;
    private final DLButton deleteButton;
    private final DLButton moveUpButton;
    private final DLButton moveDownButton;
    private final DLIconButton[] signalButtons;
    private GuiAreaDefinition signalSelectionArea = GuiAreaDefinition.empty();

    //private final TrafficLightType type = TrafficLightType.CAR;
    // Animation
    private static final int FRAME_DURATION_TICKS = 20;
    private int ticks;
    private int frame;
    private TrafficLightType typeFrame = TrafficLightType.CAR;

    private final TrafficLightColor[] signals;
    private final Collection<DLTooltip> tooltips = new ArrayList<>();

    // texts
    private static final Component textDelay = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delay");
    private static final Component textPhaseId = TextUtils.translate("gui.trafficcraft.trafficlightschedule.phase_id");
    private static final Component textMoveUp = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_up");
    private static final Component textMoveDown = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_down");
    private static final Component textDelete = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delete");

    // data
    private final boolean hidePhaseId;
    private final TrafficLightScheduleEntry instance = this;
    private DLContextMenu menu;

    public TrafficLightScheduleEntry(TrafficLightScheduleContainer container, Map<Integer, TrafficLightType> signalTypes, boolean hidePhaseId, TrafficLightScheduleEntryData entry, int x, int y, int pWidth, Consumer<TrafficLightScheduleEntryData> removeAction, BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction) {
        super(x, y, pWidth, HEIGHT);
        this.parent = container;
        this.hidePhaseId = hidePhaseId;

        delaySelector = addRenderableWidget(new DLNumberSelector(
            x() + LEFT_PADDING + TIMELINE_WIDTH + 1,
            y() + SPACING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            38,
            DEFAULT_EDIT_BOX_HEIGHT,
            entry.getDurationSeconds(),
            true,
            (selector, value) -> {
                entry.setDurationSeconds(MathUtils.clamp(selector.getAsInt(), 0, TrafficLightScheduleEntryData.MAX_SECONDS));
            }
        ) {
            @Override
            public void setMouseSelected(boolean selected) {
                super.setMouseSelected(selected);
                instance.setMouseSelected(selected);
            }
        });
        delaySelector.setNumberBounds(0, TrafficLightScheduleEntryData.MAX_SECONDS);
        DLTooltip tooltip1 = DLTooltip.of(textDelay).assignedTo(GuiAreaDefinition.of(delaySelector));
        tooltip1.setDynamicOffset(() -> 0, () -> (int)container.getScrollOffset());
        tooltips.add(tooltip1);
        widgets.add(delaySelector);

        int signalSelectionX = x() + LEFT_PADDING + TIMELINE_WIDTH;
        if (!hidePhaseId) {
            phaseIdBox = addRenderableWidget(new DLNumberSelector(
                x() + LEFT_PADDING + TIMELINE_WIDTH + 1,
                y() + (int)(SPACING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - (DEFAULT_EDIT_BOX_HEIGHT - 2) / 2),
                38,
                DEFAULT_EDIT_BOX_HEIGHT - 2,
                entry.getPhaseId(),
                false,
                (selector, value) -> {
                    entry.setPhaseId(selector.getAsInt());
                }
            ) {
                @Override
                public void setMouseSelected(boolean selected) {
                    super.setMouseSelected(selected);
                    instance.setMouseSelected(selected);
                }
            });
            phaseIdBox.setNumberBounds(-9999, 9999);
            DLTooltip tooltip2 = DLTooltip.of(textPhaseId).assignedTo(GuiAreaDefinition.of(phaseIdBox));
            tooltip2.setDynamicOffset(() -> 0, () -> (int)container.getScrollOffset());
            tooltips.add(tooltip2); 
            widgets.add(phaseIdBox);
            
            signalSelectionX += phaseIdBox.getWidth() + 6;
        } else {
            phaseIdBox = null;
        }

        signals = TrafficLightColor.getAllowedForType(TrafficLightType.CAR, false);
        signalSelectionArea = new GuiAreaDefinition(signalSelectionX, y() + (int)(SPACING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1), signals.length * (SIGNAL_ICON_SIZE + 4) + 4, DEFAULT_EDIT_BOX_HEIGHT - 2);
        this.signalButtons = new DLIconButton[signals.length];
        for (int i = 0; i < signals.length; i++) {
            final int j = i;
            this.signalButtons[j] = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.FLAT, Sprite.empty(), signalSelectionArea.getX() + 3 + j * (SIGNAL_ICON_SIZE + 4), signalSelectionArea.getY() + 2, SIGNAL_ICON_SIZE + 2, SIGNAL_ICON_SIZE + 2, TextUtils.empty(),
            (b) -> {
                if (entry.getEnabledColors().contains(signals[j])) {
                    entry.disableColors(List.of(signals[j]));
                } else {
                    entry.enableColors(List.of(signals[j]));
                }
            }) {
                @Override
                public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
                    GuiUtils.fill(graphics, x(), y(), width(), height(), isMouseSelected() ? 0xFFFFFFFF : 0xFFA7A7A7);
                    GuiUtils.drawTexture(
                        TrafficLightScheduleEditor.WIDGETS,
                        graphics,
                        signalButtons[j].x() + 1,
                        signalButtons[j].y() + 1,
                        SIGNAL_ICON_SIZE,
                        SIGNAL_ICON_SIZE,
                        j * SIGNAL_ICON_SIZE,
                        (signalTypes.containsKey(entry.getPhaseId()) && signalTypes.get(entry.getPhaseId()) != null ? signalTypes.get(entry.getPhaseId()).getIndex() : typeFrame.getIndex()) * SIGNAL_ICON_SIZE,
                        SIGNAL_ICON_SIZE,
                        SIGNAL_ICON_SIZE,
                        TrafficLightScheduleEditor.TEXTURE_WIDTH,
                        TrafficLightScheduleEditor.TEXTURE_HEIGHT
                    );
                    if (!entry.getEnabledColors().contains(signals[j])) {
                        GuiUtils.fill(graphics, x() + 1, y() + 1, width() - 2, height() - 2, 0xAA000000);
                    }   
                }

                @Override
                public void setMouseSelected(boolean selected) {
                    super.setMouseSelected(selected);
                    instance.setMouseSelected(selected);
                }
            });
        }

        moveUpButton = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.FLAT, MOVE_UP_ICON, x() + width() - CONTROL_BUTTON_SIZE - 4, y() + 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, TextUtils.empty(),
        (b) -> {
            reorderAction.accept(entry, -1);
        }) {
            @Override
            public void setMouseSelected(boolean selected) {
                super.setMouseSelected(selected);
                instance.setMouseSelected(selected);
            }
        });
        moveUpButton.setRenderStyle(AreaStyle.FLAT);
        moveUpButton.setBackColor(0x00000000);
        DLTooltip tooltip3 = DLTooltip.of(textMoveUp).assignedTo(GuiAreaDefinition.of(moveUpButton));
        tooltip3.setDynamicOffset(() -> 0, () -> (int)container.getScrollOffset());
        tooltips.add(tooltip3);

        moveDownButton = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.FLAT, MOVE_DOWN_ICON, x() + width() - CONTROL_BUTTON_SIZE - 4, y() + 4 + CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, TextUtils.empty(),
        (b) -> {
            reorderAction.accept(entry, 1);
        }) {
            @Override
            public void setMouseSelected(boolean selected) {
                super.setMouseSelected(selected);
                instance.setMouseSelected(selected);
            }
        });
        moveDownButton.setRenderStyle(AreaStyle.FLAT);
        moveDownButton.setBackColor(0x00000000);
        DLTooltip tooltip4 = DLTooltip.of(textMoveDown).assignedTo(GuiAreaDefinition.of(moveDownButton));
        tooltip4.setDynamicOffset(() -> 0, () -> (int)container.getScrollOffset());
        tooltips.add(tooltip4);

        deleteButton = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.FLAT, DELETE_ICON, x() + width() - CONTROL_BUTTON_SIZE - 4, y() + height - CONTROL_BUTTON_SIZE - 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, TextUtils.empty(),
        (b) -> {
            removeAction.accept(entry);
        }) {
            @Override
            public void setMouseSelected(boolean selected) {
                super.setMouseSelected(selected);
                instance.setMouseSelected(selected);
            }
        });
        deleteButton.setRenderStyle(AreaStyle.FLAT);
        deleteButton.setBackColor(0x00000000);
        DLTooltip tooltip5 = DLTooltip.of(textDelete).assignedTo(GuiAreaDefinition.of(deleteButton));
        tooltip5.setDynamicOffset(() -> 0, () -> (int)container.getScrollOffset());
        tooltips.add(tooltip5);
        
        
        setMenu(new DLContextMenu(() -> GuiAreaDefinition.of(this), () ->
            new DLContextMenuItem.Builder()
                .add(new ContextMenuItemData(textMoveUp, Sprite.empty(), true, (b) -> reorderAction.accept(entry, -1), null))
                .add(new ContextMenuItemData(textMoveDown, Sprite.empty(), true, (b) -> reorderAction.accept(entry, 1), null))
                .addSeparator()
                .add(new ContextMenuItemData(textDelete, Sprite.empty(), true, (b) -> removeAction.accept(entry), null))
        ));
        
    }

    @Override
    public DLContextMenu getContextMenu() {
        return menu;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    public void tick() {
        // Animation
        ticks++;
        if (ticks % FRAME_DURATION_TICKS == 0) {
            frame++;
            typeFrame = TrafficLightType.getTypeByIndex((byte)(frame % TrafficLightType.values().length));
        }
    }

    @Override
    public void setMouseSelected(boolean selected) {        
        super.setMouseSelected(selected);
        moveUpButton.set_visible(isMouseSelected());
        moveDownButton.set_visible(isMouseSelected());
        deleteButton.set_visible(isMouseSelected());
    }

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        
        if (isMouseSelected()) {            
            GuiUtils.fill(graphics, x, y, width, height, 0x22FFFFFF);
        }
        
        DynamicGuiRenderer.renderArea(
            graphics,
            x() + LEFT_PADDING,
            y() + SPACING / 2,
            TIMELINE_WIDTH + delaySelector.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        DynamicGuiRenderer.renderArea(
            graphics,
            x + LEFT_PADDING,
            y + (int)(SPACING * 1.5f + DEFAULT_ENTRY_HEIGHT),
            TIMELINE_WIDTH + (hidePhaseId ? 0 : phaseIdBox.getWidth() + 6) + signalSelectionArea.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        GuiUtils.drawTexture(
            TrafficLightScheduleEditor.WIDGETS,
            graphics, x + TrafficLightScheduleEditor.ENTRY_PADDING + TIMELINE_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            y,
            TrafficLightScheduleEditor.TIMELINE_UW,
            HEIGHT,
            27,
            20,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            TrafficLightScheduleEditor.TEXTURE_WIDTH,
            TrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.drawTexture(
            TrafficLightScheduleEditor.WIDGETS,
            graphics,
            x + TrafficLightScheduleEditor.ENTRY_PADDING + TIMELINE_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - TrafficLightScheduleEditor.TIMELINE_VH / 2,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            18,
            20,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            TrafficLightScheduleEditor.TEXTURE_WIDTH,
            TrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.drawTexture(
            TrafficLightScheduleEditor.WIDGETS,
            graphics,
            x + TrafficLightScheduleEditor.ENTRY_PADDING + TIMELINE_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            y + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - TrafficLightScheduleEditor.TIMELINE_VH / 2),
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            9,
            20,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            TrafficLightScheduleEditor.TEXTURE_WIDTH,
            TrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.drawBox(graphics, signalSelectionArea, 0xFF000000, 0xFFDBDBDB);

        super.renderMainLayer(graphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        if (this.parent.isMouseOver(mouseX, mouseY))
            tooltips.stream().forEach(x -> x.render(Minecraft.getInstance().screen, graphics, mouseX, mouseY));
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) { }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return false;
    }    

}
