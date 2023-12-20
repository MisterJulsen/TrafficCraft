package de.mrjulsen.trafficcraft.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableButton;
import de.mrjulsen.mcdragonlib.utils.Math;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.ModGuiUtils;
import de.mrjulsen.trafficcraft.client.screen.NewTrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightAnimationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;

public class NewTrafficLightScheduleEntry extends Button {

    private static final int DEFAULT_EDIT_BOX_HEIGHT = 16;
    private static final int DEFAULT_ENTRY_HEIGHT = 22;
    private static final int CONTROL_BUTTON_SIZE = 16;
    private static final int CONTROL_BUTTON_IMAGE_SIZE = 12;
    private static final int SIGNAL_ICON_SIZE = 10;
    public static final int HEIGHT = NewTrafficLightScheduleEditor.ENTRY_PADDING * 2 + DEFAULT_ENTRY_HEIGHT * 2;

    private final WidgetsCollection widgets = new WidgetsCollection();

    private final EditBox delayBox;
    private final ResizableButton addTimeButton;
    private final ResizableButton removeTimeButton;

    private GuiAreaDefinition deleteButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition moveUpButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition moveDownButton = GuiAreaDefinition.empty();
    private GuiAreaDefinition signalSelectionArea = GuiAreaDefinition.empty();
    private GuiAreaDefinition[] signalAreas = new GuiAreaDefinition[0];

    private final TrafficLightType type = TrafficLightType.CAR;
    private final TrafficLightColor[] signals;

    private final Screen parent;
    private final EditBox phaseIdBox;

    private final Consumer<TrafficLightAnimationData> removeAction; 
    private final BiConsumer<TrafficLightAnimationData, Integer> reorderAction; 

    // texts
    private static final Component textDelay = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.delay");
    private static final Component textAddTime = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.add_time");
    private static final Component textRemoveTime = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.remove_time");
    private static final Component textPhaseId = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.phase_id");
    private static final Component textMoveUp = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.move_up");
    private static final Component textMoveDown = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.move_down");
    private static final Component textDelete = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.delete");

    private final List<Tooltip> widgetTooltips = new ArrayList<>();
    private final List<Tooltip> areaTooltips = new ArrayList<>();

    // data
    private final TrafficLightAnimationData entry;
    private final boolean hidePhaseId;

    public NewTrafficLightScheduleEntry(Screen parent, boolean hidePhaseId, TrafficLightAnimationData entry, int pX, int pY, int pWidth, Consumer<TrafficLightAnimationData> removeAction, BiConsumer<TrafficLightAnimationData, Integer> reorderAction) {
        super(pX, pY, pWidth, HEIGHT, TextComponent.EMPTY, null);
        this.hidePhaseId = hidePhaseId;

        Minecraft minecraft = Minecraft.getInstance();
        this.parent = parent;
        this.entry = entry;
        this.removeAction = removeAction;
        this.reorderAction = reorderAction;

        delayBox = GuiUtils.createEditBox(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 1,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1,
            38,
            DEFAULT_EDIT_BOX_HEIGHT - 2,
            minecraft.font,
            String.valueOf((int)entry.getDurationSeconds()),
            true,
            (text) -> {
                try {
                    entry.setDurationSeconds(Math.clamp(Integer.parseInt(text), 0, TrafficLightAnimationData.MAX_SECONDS));
                } catch (Exception e) {}
            },
            (box, focus) -> {}
        );
        delayBox.setFilter(ModGuiUtils::editBoxNonNegativeNumberFilter);
        delayBox.setMaxLength(String.valueOf(TrafficLightAnimationData.MAX_SECONDS).length());
        widgetTooltips.add(Tooltip.of(textDelay).assignedTo(delayBox));

        removeTimeButton = GuiUtils.createButton(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            GuiUtils.text("-"),
            (btn) -> {
                if (entry.getDurationSeconds() <= 0) {
                    return;
                }
                int val = (int)entry.getDurationSeconds() - 1;
                delayBox.setValue(String.valueOf(val));
                entry.setDurationSeconds(val);
            }
        );
        widgetTooltips.add(Tooltip.of(textRemoveTime).assignedTo(removeTimeButton));

        addTimeButton = GuiUtils.createButton(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 40,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            GuiUtils.text("+"),
            (btn) -> {
                if (entry.getDurationSeconds() >= TrafficLightAnimationData.MAX_SECONDS) {
                    return;
                }
                int val = (int)entry.getDurationSeconds() + 1;
                delayBox.setValue(String.valueOf(val));
                entry.setDurationSeconds(val);
            }
        );
        widgetTooltips.add(Tooltip.of(textAddTime).assignedTo(addTimeButton));

        widgets.add(delayBox);
        widgets.add(addTimeButton);
        widgets.add(removeTimeButton);

        if (!hidePhaseId) {
            phaseIdBox = GuiUtils.createEditBox(
                pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
                (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2),
                38,
                DEFAULT_EDIT_BOX_HEIGHT - 2,
                minecraft.font,
                String.valueOf(entry.getPhaseId()),
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
            widgetTooltips.add(Tooltip.of(textPhaseId).assignedTo(phaseIdBox)); 
            widgets.add(phaseIdBox);
        } else {
            phaseIdBox = null;
        }
        
        signals = TrafficLightColor.getAllowedForType(type, false);
    }

    public void setY(int y) {
        if (this.y == y) {
            return;
        }

        areaTooltips.clear();

        delayBox.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1;
        addTimeButton.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;
        removeTimeButton.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;

        int phaseIdBoxY = y + (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1);
        int signalSelectionX = x + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH;
        if (!hidePhaseId) {
            phaseIdBox.y = phaseIdBoxY;
            signalSelectionX += phaseIdBox.getWidth() + 6;
        }

        this.y = y;

        moveUpButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        moveDownButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + 4 + CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        deleteButton = new GuiAreaDefinition(x + width - CONTROL_BUTTON_SIZE - 4, y + height - CONTROL_BUTTON_SIZE - 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        signalSelectionArea = new GuiAreaDefinition(signalSelectionX, phaseIdBoxY - 1, signals.length * (SIGNAL_ICON_SIZE + 4) + 4, DEFAULT_EDIT_BOX_HEIGHT);
        
        areaTooltips.add(Tooltip.of(textMoveUp).assignedTo(moveUpButton));
        areaTooltips.add(Tooltip.of(textMoveDown).assignedTo(moveDownButton));
        areaTooltips.add(Tooltip.of(textDelete).assignedTo(deleteButton));

        this.signalAreas = new GuiAreaDefinition[signals.length];
        for (int i = 0; i < signals.length; i++) {
            this.signalAreas[i] = new GuiAreaDefinition(signalSelectionArea.getLeft() + 3 + i * (SIGNAL_ICON_SIZE + 4), signalSelectionArea.getTop() + 2, SIGNAL_ICON_SIZE + 2, SIGNAL_ICON_SIZE + 2);
        }
    }

    public void tick() {
        widgets.performForEachOfType(EditBox.class, x -> x.visible, x -> x.tick());
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {        
        DynamicGuiRenderer.renderArea(
            pPoseStack,
            x + NewTrafficLightScheduleEditor.ENTRY_PADDING,
            y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2,
            NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + removeTimeButton.getWidth() + delayBox.getWidth() + 2 + addTimeButton.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        DynamicGuiRenderer.renderArea(
            pPoseStack,
            x + NewTrafficLightScheduleEditor.ENTRY_PADDING,
            y + (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT),
            NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + (hidePhaseId ? 0 : phaseIdBox.getWidth() + 6) + signalSelectionArea.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        GuiUtils.blit(
            NewTrafficLightScheduleEditor.WIDGETS,
            pPoseStack, x + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - NewTrafficLightScheduleEditor.TIMELINE_UW / 2,
            y,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            HEIGHT,
            27,
            20,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            NewTrafficLightScheduleEditor.TEXTURE_WIDTH,
            NewTrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.blit(
            NewTrafficLightScheduleEditor.WIDGETS,
            pPoseStack,
            x + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - NewTrafficLightScheduleEditor.TIMELINE_UW / 2,
            y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - NewTrafficLightScheduleEditor.TIMELINE_VH / 2,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            18,
            20,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            NewTrafficLightScheduleEditor.TEXTURE_WIDTH,
            NewTrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.blit(
            NewTrafficLightScheduleEditor.WIDGETS,
            pPoseStack,
            x + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - NewTrafficLightScheduleEditor.TIMELINE_UW / 2,
            y + (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - NewTrafficLightScheduleEditor.TIMELINE_VH / 2),
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            9,
            20,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            NewTrafficLightScheduleEditor.TEXTURE_WIDTH,
            NewTrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        if (isMouseOver(pMouseX, pMouseY)) {
            fill(pPoseStack, x + 1, y, x + 1 + width, y + height, 0x22FFFFFF);

            GuiUtils.blit(NewTrafficLightScheduleEditor.WIDGETS, pPoseStack, moveUpButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveUpButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE * 2, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, NewTrafficLightScheduleEditor.TEXTURE_WIDTH, NewTrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(NewTrafficLightScheduleEditor.WIDGETS, pPoseStack, moveDownButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveDownButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, NewTrafficLightScheduleEditor.TEXTURE_WIDTH, NewTrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(NewTrafficLightScheduleEditor.WIDGETS, pPoseStack, deleteButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, deleteButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 0, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, NewTrafficLightScheduleEditor.TEXTURE_WIDTH, NewTrafficLightScheduleEditor.TEXTURE_HEIGHT);

            if (moveUpButton.isInBounds(pMouseX, pMouseY)) {
                fill(pPoseStack, moveUpButton.getLeft(), moveUpButton.getTop(), moveUpButton.getRight(), moveUpButton.getBottom(), 0x22FFFFFF);
            } else if (moveDownButton.isInBounds(pMouseX, pMouseY)) {
                fill(pPoseStack, moveDownButton.getLeft(), moveDownButton.getTop(), moveDownButton.getRight(), moveDownButton.getBottom(), 0x22FFFFFF);
            } else if (deleteButton.isInBounds(pMouseX, pMouseY)) {
                fill(pPoseStack, deleteButton.getLeft(), deleteButton.getTop(), deleteButton.getRight(), deleteButton.getBottom(), 0x22FFFFFF);
            }
        }

        fill(pPoseStack, signalSelectionArea.getLeft(), signalSelectionArea.getTop(), signalSelectionArea.getRight(), signalSelectionArea.getBottom(), 0xFFDBDBDB);
        fill(pPoseStack, signalSelectionArea.getLeft() + 1, signalSelectionArea.getTop() + 1, signalSelectionArea.getRight() - 1, signalSelectionArea.getBottom() - 1, 0xFF000000);

        for (int i = 0; i < signalAreas.length; i++) {
            fill(pPoseStack, signalAreas[i].getLeft(), signalAreas[i].getTop(), signalAreas[i].getRight(), signalAreas[i].getBottom(), signalAreas[i].isInBounds(pMouseX, pMouseY) ? 0xFFFFFFFF : 0xFFA7A7A7);
            GuiUtils.blit(
                NewTrafficLightScheduleEditor.WIDGETS,
                pPoseStack,
                signalAreas[i].getLeft() + 1,
                signalAreas[i].getTop() + 1,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                i * SIGNAL_ICON_SIZE,
                type.getIndex() * SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                NewTrafficLightScheduleEditor.TEXTURE_WIDTH,
                NewTrafficLightScheduleEditor.TEXTURE_HEIGHT
            );

            if (!entry.getEnabledColors().contains(signals[i])) {
                fill(pPoseStack, signalAreas[i].getLeft() + 1, signalAreas[i].getTop() + 1, signalAreas[i].getRight() - 1, signalAreas[i].getBottom() - 1, 0xAA000000);
            }
        }

        widgets.performForEach(x -> x.visible, x -> x.render(pPoseStack, pMouseX, pMouseY, pPartialTick));
    }

    public void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int offset) {
        // TODO: manuell bounding box checken, damit nur beim checken der offset verrechnet wird, aber nicht beim rendern.
        widgetTooltips.forEach(x -> x.render(parent, pPoseStack, pMouseX, pMouseY));
        areaTooltips.forEach(x -> x.render(parent, pPoseStack, pMouseX, pMouseY));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        widgets.performForEach(x -> x.visible, x -> x.mouseClicked(pMouseX, pMouseY, pButton));

        for (int i = 0; i < signalAreas.length && i < signals.length; i++) {
            if (signalAreas[i].isInBounds(pMouseX, pMouseY)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (entry.getEnabledColors().contains(signals[i])) {
                    entry.disableColors(List.of(signals[i]));
                } else {
                    entry.enableColors(List.of(signals[i]));
                }
                int[] a = entry.getEnabledColors().stream().mapToInt(x -> x.getIndex()).toArray();
                for (int x : a) {
                    System.out.print(x + ", ");
                }
                System.out.println();
                return true;
            }
        }

        if (moveUpButton.isInBounds(pMouseX, pMouseY)) {
            reorderAction.accept(entry, -1);
            return true;
        } else if (moveDownButton.isInBounds(pMouseX, pMouseY)) {
            reorderAction.accept(entry, 1);
            return true;
        } else if (deleteButton.isInBounds(pMouseX, pMouseY)) {
            removeAction.accept(entry);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        widgets.performForEach(x -> x.visible, x -> x.keyPressed(pKeyCode, pScanCode, pModifiers));
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        widgets.performForEach(x -> x.visible, x -> x.charTyped(pCodePoint, pModifiers));
        return super.charTyped(pCodePoint, pModifiers);
    }    
}
