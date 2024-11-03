package de.mrjulsen.trafficcraft.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.WidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.ModGuiUtils;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;

public class TrafficLightScheduleEntry extends WidgetContainer {

    private static final int DEFAULT_EDIT_BOX_HEIGHT = 16;
    private static final int DEFAULT_ENTRY_HEIGHT = 22;
    private static final int CONTROL_BUTTON_SIZE = 16;
    private static final int CONTROL_BUTTON_IMAGE_SIZE = 12;
    private static final int SIGNAL_ICON_SIZE = 10;
    public static final int HEIGHT = TrafficLightScheduleEditor.ENTRY_PADDING * 2 + DEFAULT_ENTRY_HEIGHT * 2;

    private final WidgetsCollection widgets = new WidgetsCollection();

    private final EditBox delayBox;
    private final DLButton addTimeButton;
    private final DLButton removeTimeButton;

    private GuiAreaDefinition deleteButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition moveUpButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition moveDownButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition signalSelectionArea = GuiAreaDefinition.empty();
    private GuiAreaDefinition[] signalAreas = new GuiAreaDefinition[0];

    //private final TrafficLightType type = TrafficLightType.CAR;
    // Animation
    private static final int FRAME_DURATION_TICKS = 20;
    private int ticks;
    private int frame;
    private TrafficLightType typeFrame = TrafficLightType.CAR;

    private final TrafficLightColor[] signals;

    private final TrafficLightScheduleEditor parent;
    private final DLEditBox phaseIdBox;

    private final Consumer<TrafficLightScheduleEntryData> removeAction; 
    private final BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction; 

    // texts
    private static final Component textDelay = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delay");
    private static final Component textAddTime = TextUtils.translate("gui.trafficcraft.trafficlightschedule.add_time");
    private static final Component textRemoveTime = TextUtils.translate("gui.trafficcraft.trafficlightschedule.remove_time");
    private static final Component textPhaseId = TextUtils.translate("gui.trafficcraft.trafficlightschedule.phase_id");
    private static final Component textMoveUp = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_up");
    private static final Component textMoveDown = TextUtils.translate("gui.trafficcraft.trafficlightschedule.move_down");
    private static final Component textDelete = TextUtils.translate("gui.trafficcraft.trafficlightschedule.delete");

    private final List<DLTooltip> widgetTooltips = new ArrayList<>();
    private final List<DLTooltip> areaTooltips = new ArrayList<>();

    // data
    private final TrafficLightScheduleEntryData entry;
    private final boolean hidePhaseId;

    public TrafficLightScheduleEntry(TrafficLightScheduleEditor parent, boolean hidePhaseId, TrafficLightScheduleEntryData entry, int pX, int pY, int pWidth, Consumer<TrafficLightScheduleEntryData> removeAction, BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction) {
        super(pX, pY, pWidth, HEIGHT);
        this.hidePhaseId = hidePhaseId;

        Minecraft minecraft = Minecraft.getInstance();
        this.parent = parent;
        this.entry = entry;
        this.removeAction = removeAction;
        this.reorderAction = reorderAction;

        delayBox = GuiUtils.createEditBox(
            pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 1,
            TrafficLightScheduleEditor.ENTRY_PADDING / 2 + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1,
            38,
            DEFAULT_EDIT_BOX_HEIGHT - 2,
            minecraft.font,
            String.valueOf((int)entry.getDurationSeconds()),
            TextUtils.empty(),
            true,
            (text) -> {
                try {
                    entry.setDurationSeconds(MathUtils.clamp(Integer.parseInt(text), 0, TrafficLightScheduleEntryData.MAX_SECONDS));
                } catch (Exception e) {}
            },
            (box, focus) -> {}
        );
        delayBox.setFilter(ModGuiUtils::editBoxNonNegativeNumberFilter);
        delayBox.setMaxLength(String.valueOf(TrafficLightScheduleEntryData.MAX_SECONDS).length());
        widgetTooltips.add(DLTooltip.of(textDelay).assignedTo(delayBox));

        removeTimeButton = GuiUtils.createButton(
            pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
            TrafficLightScheduleEditor.ENTRY_PADDING / 2 + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            TextUtils.text("-"),
            (btn) -> {
                if (entry.getDurationSeconds() <= 0) {
                    return;
                }
                int val = (int)entry.getDurationSeconds() - 1;
                delayBox.setValue(String.valueOf(val));
                entry.setDurationSeconds(val);
            }
        );
        widgetTooltips.add(DLTooltip.of(textRemoveTime).assignedTo(removeTimeButton));

        addTimeButton = GuiUtils.createButton(
            pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 40,
            TrafficLightScheduleEditor.ENTRY_PADDING / 2 + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            TextUtils.text("+"),
            (btn) -> {
                if (entry.getDurationSeconds() >= TrafficLightScheduleEntryData.MAX_SECONDS) {
                    return;
                }
                int val = (int)entry.getDurationSeconds() + 1;
                delayBox.setValue(String.valueOf(val));
                entry.setDurationSeconds(val);
            }
        );
        widgetTooltips.add(DLTooltip.of(textAddTime).assignedTo(addTimeButton));

        widgets.add(delayBox);
        widgets.add(addTimeButton);
        widgets.add(removeTimeButton);

        if (!hidePhaseId) {
            phaseIdBox = GuiUtils.createEditBox(
                pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
                (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2),
                38,
                DEFAULT_EDIT_BOX_HEIGHT - 2,
                minecraft.font,
                String.valueOf(entry.getPhaseId()),
                TextUtils.empty(),
                true,
                (text) -> {
                    try {
                        entry.setPhaseId(Integer.parseInt(text));
                    } catch (Exception e) {}
                },
                (box, focus) -> {}
            );
            phaseIdBox.setFilter(GuiUtils::editBoxNumberFilter);
            phaseIdBox.setMaxLength(4);
            widgetTooltips.add(DLTooltip.of(textPhaseId).assignedTo(phaseIdBox)); 
            widgets.add(phaseIdBox);
        } else {
            phaseIdBox = null;
        }
        
        signals = TrafficLightColor.getAllowedForType(TrafficLightType.CAR, false);
    }

    public void setY(int y) {
        if (this.y == y) {
            return;
        }

        areaTooltips.clear();

        delayBox.y = y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1;
        addTimeButton.y = y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;
        removeTimeButton.y = y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;

        int phaseIdBoxY = y + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1);
        int signalSelectionX = x + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH;
        if (!hidePhaseId) {
            phaseIdBox.y = phaseIdBoxY;
            signalSelectionX += phaseIdBox.getWidth() + 6;
        }

        this.y = y;

        moveUpButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        moveDownButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + 4 + CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        deleteButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + height - CONTROL_BUTTON_SIZE - 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        signalSelectionArea = new GuiAreaDefinition(signalSelectionX, phaseIdBoxY - 1, signals.length * (SIGNAL_ICON_SIZE + 4) + 4, DEFAULT_EDIT_BOX_HEIGHT);
        
        areaTooltips.add(DLTooltip.of(textMoveUp).assignedTo(moveUpButton));
        areaTooltips.add(DLTooltip.of(textMoveDown).assignedTo(moveDownButton));
        areaTooltips.add(DLTooltip.of(textDelete).assignedTo(deleteButton));

        this.signalAreas = new GuiAreaDefinition[signals.length];
        for (int i = 0; i < signals.length; i++) {
            this.signalAreas[i] = new GuiAreaDefinition(signalSelectionArea.getLeft() + 3 + i * (SIGNAL_ICON_SIZE + 4), signalSelectionArea.getTop() + 2, SIGNAL_ICON_SIZE + 2, SIGNAL_ICON_SIZE + 2);
        }
    }

    public void tick() {
        widgets.performForEachOfType(EditBox.class, x -> x.visible, x -> x.tick());

        // Animation
        ticks++;
        if (ticks % FRAME_DURATION_TICKS == 0) {
            frame++;
            typeFrame = TrafficLightType.getTypeByIndex((byte)(frame % TrafficLightType.values().length));
        }
    }
    
    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        DynamicGuiRenderer.renderArea(
            graphics,
            x + TrafficLightScheduleEditor.ENTRY_PADDING,
            y + TrafficLightScheduleEditor.ENTRY_PADDING / 2,
            TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + removeTimeButton.getWidth() + delayBox.getWidth() + 2 + addTimeButton.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        DynamicGuiRenderer.renderArea(
            graphics,
            x + TrafficLightScheduleEditor.ENTRY_PADDING,
            y + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT),
            TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + (hidePhaseId ? 0 : phaseIdBox.getWidth() + 6) + signalSelectionArea.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        GuiUtils.drawTexture(
            TrafficLightScheduleEditor.WIDGETS,
            graphics, x + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
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
            x + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
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
            x + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
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

        if (isMouseOver(pMouseX, pMouseY)) {
            fill(graphics, x + 1, y, x + 1 + width, y + height, 0x22FFFFFF);

            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, graphics, moveUpButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveUpButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE * 2, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, graphics, moveDownButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveDownButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, graphics, deleteButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, deleteButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 0, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);

            if (moveUpButton.isInBounds(pMouseX, pMouseY)) {
                fill(graphics, moveUpButton.getLeft(), moveUpButton.getTop(), moveUpButton.getRight(), moveUpButton.getBottom(), 0x22FFFFFF);
            } else if (moveDownButton.isInBounds(pMouseX, pMouseY)) {
                fill(graphics, moveDownButton.getLeft(), moveDownButton.getTop(), moveDownButton.getRight(), moveDownButton.getBottom(), 0x22FFFFFF);
            } else if (deleteButton.isInBounds(pMouseX, pMouseY)) {
                fill(graphics, deleteButton.getLeft(), deleteButton.getTop(), deleteButton.getRight(), deleteButton.getBottom(), 0x22FFFFFF);
            }
        }

        fill(graphics, signalSelectionArea.getLeft(), signalSelectionArea.getTop(), signalSelectionArea.getRight(), signalSelectionArea.getBottom(), 0xFFDBDBDB);
        fill(graphics, signalSelectionArea.getLeft() + 1, signalSelectionArea.getTop() + 1, signalSelectionArea.getRight() - 1, signalSelectionArea.getBottom() - 1, 0xFF000000);

        for (int i = 0; i < signalAreas.length; i++) {
            fill(graphics, signalAreas[i].getLeft(), signalAreas[i].getTop(), signalAreas[i].getRight(), signalAreas[i].getBottom(), signalAreas[i].isInBounds(pMouseX, pMouseY) ? 0xFFFFFFFF : 0xFFA7A7A7);
            GuiUtils.blit(
                TrafficLightScheduleEditor.WIDGETS,
                graphics,
                signalAreas[i].getLeft() + 1,
                signalAreas[i].getTop() + 1,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                i * SIGNAL_ICON_SIZE,
                (parent.getPhaseTypes().containsKey(entry.getPhaseId()) && parent.getPhaseTypes().get(entry.getPhaseId()) != null ? parent.getPhaseTypes().get(entry.getPhaseId()).getIndex() : typeFrame.getIndex()) * SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                TrafficLightScheduleEditor.TEXTURE_WIDTH,
                TrafficLightScheduleEditor.TEXTURE_HEIGHT
            );

            if (!entry.getEnabledColors().contains(signals[i])) {
                fill(graphics, signalAreas[i].getLeft() + 1, signalAreas[i].getTop() + 1, signalAreas[i].getRight() - 1, signalAreas[i].getBottom() - 1, 0xAA000000);
            }
        }

        widgets.performForEach(x -> x.visible, x -> x.render(graphics, pMouseX, pMouseY, pPartialTick));
    }

    public void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int offset) {
        widgetTooltips.forEach(x -> {
            x.render(parent, pPoseStack, pMouseX, pMouseY, 0, offset);
        });
        areaTooltips.forEach(x -> x.render(parent, pPoseStack, pMouseX, pMouseY, 0, offset));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (widgets.components.stream().anyMatch(x -> x.mouseClicked(pMouseX, pMouseY, pButton))) {
            return true;
        }

        for (int i = 0; i < signalAreas.length && i < signals.length; i++) {
            if (signalAreas[i].isInBounds(pMouseX, pMouseY)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (entry.getEnabledColors().contains(signals[i])) {
                    entry.disableColors(List.of(signals[i]));
                } else {
                    entry.enableColors(List.of(signals[i]));
                }
                return true;
            }
        }

        if (moveUpButton.isInBounds(pMouseX, pMouseY)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            reorderAction.accept(entry, -1);
            return true;
        } else if (moveDownButton.isInBounds(pMouseX, pMouseY)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            reorderAction.accept(entry, 1);
            return true;
        } else if (deleteButton.isInBounds(pMouseX, pMouseY)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            removeAction.accept(entry);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (widgets.components.stream().anyMatch(x -> x.keyPressed(pKeyCode, pScanCode, pModifiers))) {
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (widgets.components.stream().anyMatch(x -> x.charTyped(pCodePoint, pModifiers))) {
            return true;
        }
        
        return super.charTyped(pCodePoint, pModifiers);
    }    
}
