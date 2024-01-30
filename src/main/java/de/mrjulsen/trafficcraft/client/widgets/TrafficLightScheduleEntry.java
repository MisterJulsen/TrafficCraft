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
import de.mrjulsen.mcdragonlib.utils.Utils;
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
import net.minecraft.sounds.SoundEvents;

public class TrafficLightScheduleEntry extends Button {

    private static final int DEFAULT_EDIT_BOX_HEIGHT = 16;
    private static final int DEFAULT_ENTRY_HEIGHT = 22;
    private static final int CONTROL_BUTTON_SIZE = 16;
    private static final int CONTROL_BUTTON_IMAGE_SIZE = 12;
    private static final int SIGNAL_ICON_SIZE = 10;
    public static final int HEIGHT = TrafficLightScheduleEditor.ENTRY_PADDING * 2 + DEFAULT_ENTRY_HEIGHT * 2;

    private final WidgetsCollection widgets = new WidgetsCollection();

    private final EditBox delayBox;
    private final ResizableButton addTimeButton;
    private final ResizableButton removeTimeButton;

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
    private final EditBox phaseIdBox;

    private final Consumer<TrafficLightScheduleEntryData> removeAction; 
    private final BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction; 

    // texts
    private static final Component textDelay = Utils.translate("gui.trafficcraft.trafficlightschedule.delay");
    private static final Component textAddTime = Utils.translate("gui.trafficcraft.trafficlightschedule.add_time");
    private static final Component textRemoveTime = Utils.translate("gui.trafficcraft.trafficlightschedule.remove_time");
    private static final Component textPhaseId = Utils.translate("gui.trafficcraft.trafficlightschedule.phase_id");
    private static final Component textMoveUp = Utils.translate("gui.trafficcraft.trafficlightschedule.move_up");
    private static final Component textMoveDown = Utils.translate("gui.trafficcraft.trafficlightschedule.move_down");
    private static final Component textDelete = Utils.translate("gui.trafficcraft.trafficlightschedule.delete");

    private final List<Tooltip> widgetTooltips = new ArrayList<>();
    private final List<Tooltip> areaTooltips = new ArrayList<>();

    // data
    private final TrafficLightScheduleEntryData entry;
    private final boolean hidePhaseId;

    public TrafficLightScheduleEntry(TrafficLightScheduleEditor parent, boolean hidePhaseId, TrafficLightScheduleEntryData entry, int pX, int pY, int pWidth, Consumer<TrafficLightScheduleEntryData> removeAction, BiConsumer<TrafficLightScheduleEntryData, Integer> reorderAction) {
        super(pX, pY, pWidth, HEIGHT, Utils.emptyText(), (btn) -> {}, DEFAULT_NARRATION);
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
            true,
            (text) -> {
                try {
                    entry.setDurationSeconds(Math.clamp(Integer.parseInt(text), 0, TrafficLightScheduleEntryData.MAX_SECONDS));
                } catch (Exception e) {}
            },
            (box, focus) -> {}
        );
        delayBox.setFilter(ModGuiUtils::editBoxNonNegativeNumberFilter);
        delayBox.setMaxLength(String.valueOf(TrafficLightScheduleEntryData.MAX_SECONDS).length());
        widgetTooltips.add(Tooltip.of(textDelay).assignedTo(delayBox));

        removeTimeButton = GuiUtils.createButton(
            pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
            TrafficLightScheduleEditor.ENTRY_PADDING / 2 + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            Utils.text("-"),
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
            pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 40,
            TrafficLightScheduleEditor.ENTRY_PADDING / 2 + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            Utils.text("+"),
            (btn) -> {
                if (entry.getDurationSeconds() >= TrafficLightScheduleEntryData.MAX_SECONDS) {
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
                pX + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
                (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + TrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2),
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
        
        signals = TrafficLightColor.getAllowedForType(TrafficLightType.CAR, false);
    }

    public void setYPos(int y) {
        if (this.getY() == y) {
            return;
        }

        areaTooltips.clear();

        delayBox.setY(y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1);
        addTimeButton.setY(y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2);
        removeTimeButton.setY(y + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2);

        int phaseIdBoxY = y + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1);
        int signalSelectionX = getX() + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH;
        if (!hidePhaseId) {
            phaseIdBox.setY(phaseIdBoxY);
            signalSelectionX += phaseIdBox.getWidth() + 6;
        }

        this.setY(y);

        moveUpButton = new GuiAreaDefinition(getX() + width - CONTROL_BUTTON_SIZE - 4, y + 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        moveDownButton = new GuiAreaDefinition(getX() + width - CONTROL_BUTTON_SIZE - 4, y + 4 + CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
        deleteButton = new GuiAreaDefinition(getX() + width - CONTROL_BUTTON_SIZE - 4, y + height - CONTROL_BUTTON_SIZE - 4, CONTROL_BUTTON_SIZE, CONTROL_BUTTON_SIZE);
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

        // Animation
        ticks++;
        if (ticks % FRAME_DURATION_TICKS == 0) {
            frame++;
            typeFrame = TrafficLightType.getTypeByIndex((byte)(frame % TrafficLightType.values().length));
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {        
        DynamicGuiRenderer.renderArea(
            pPoseStack,
            getX() + TrafficLightScheduleEditor.ENTRY_PADDING,
            getY() + TrafficLightScheduleEditor.ENTRY_PADDING / 2,
            TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + removeTimeButton.getWidth() + delayBox.getWidth() + 2 + addTimeButton.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        DynamicGuiRenderer.renderArea(
            pPoseStack,
            getX() + TrafficLightScheduleEditor.ENTRY_PADDING,
            getY() + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT),
            TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + (hidePhaseId ? 0 : phaseIdBox.getWidth() + 6) + signalSelectionArea.getWidth() + 8,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        GuiUtils.blit(
            TrafficLightScheduleEditor.WIDGETS,
            pPoseStack, getX() + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            getY(),
            TrafficLightScheduleEditor.TIMELINE_UW,
            HEIGHT,
            27,
            20,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            TrafficLightScheduleEditor.TEXTURE_WIDTH,
            TrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.blit(
            TrafficLightScheduleEditor.WIDGETS,
            pPoseStack,
            getX() + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            getY() + TrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - TrafficLightScheduleEditor.TIMELINE_VH / 2,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            18,
            20,
            TrafficLightScheduleEditor.TIMELINE_UW,
            TrafficLightScheduleEditor.TIMELINE_VH,
            TrafficLightScheduleEditor.TEXTURE_WIDTH,
            TrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        GuiUtils.blit(
            TrafficLightScheduleEditor.WIDGETS,
            pPoseStack,
            getX() + TrafficLightScheduleEditor.ENTRY_PADDING + TrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TrafficLightScheduleEditor.TIMELINE_UW / 2,
            getY() + (int)(TrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - TrafficLightScheduleEditor.TIMELINE_VH / 2),
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
            fill(pPoseStack, getX() + 1, getY(), getX() + 1 + width, getY() + height, 0x22FFFFFF);

            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, pPoseStack, moveUpButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveUpButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE * 2, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, pPoseStack, moveDownButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, moveDownButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);
            GuiUtils.blit(TrafficLightScheduleEditor.WIDGETS, pPoseStack, deleteButton.getLeft() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, deleteButton.getTop() + (CONTROL_BUTTON_SIZE - CONTROL_BUTTON_IMAGE_SIZE) / 2, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, 0, 29, CONTROL_BUTTON_IMAGE_SIZE, CONTROL_BUTTON_IMAGE_SIZE, TrafficLightScheduleEditor.TEXTURE_WIDTH, TrafficLightScheduleEditor.TEXTURE_HEIGHT);

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
                TrafficLightScheduleEditor.WIDGETS,
                pPoseStack,
                signalAreas[i].getLeft() + 1,
                signalAreas[i].getTop() + 1,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                i * SIGNAL_ICON_SIZE,
                (parent.getPhaseTypes().containsKey(entry.getPhaseId()) ? parent.getPhaseTypes().get(entry.getPhaseId()).getIndex() : typeFrame.getIndex()) * SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                SIGNAL_ICON_SIZE,
                TrafficLightScheduleEditor.TEXTURE_WIDTH,
                TrafficLightScheduleEditor.TEXTURE_HEIGHT
            );

            if (!entry.getEnabledColors().contains(signals[i])) {
                fill(pPoseStack, signalAreas[i].getLeft() + 1, signalAreas[i].getTop() + 1, signalAreas[i].getRight() - 1, signalAreas[i].getBottom() - 1, 0xAA000000);
            }
        }

        widgets.performForEach(x -> x.visible, x -> x.render(pPoseStack, pMouseX, pMouseY, pPartialTick));
    }

    public void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int offset) {
        widgetTooltips.forEach(x -> {
            x.render(parent, pPoseStack, pMouseX, pMouseY, 0, offset);
        });
        areaTooltips.forEach(x -> x.render(parent, pPoseStack, pMouseX, pMouseY, 0, offset));
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
        boolean[] b = new boolean[] { false };
        widgets.performForEach(x -> x.visible, x -> {            
            if (b[0]) return;
            if (x.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                b[0] = true;
                return;
            }
        });
        return b[0];
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        widgets.performForEach(x -> x.visible, x -> x.charTyped(pCodePoint, pModifiers));
        return super.charTyped(pCodePoint, pModifiers);
    }    
}
