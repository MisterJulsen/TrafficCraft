package de.mrjulsen.trafficcraft.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableButton;
import de.mrjulsen.trafficcraft.client.screen.NewTrafficLightScheduleEditor;
import de.mrjulsen.trafficcraft.data.TrafficLightAnimationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class NewTrafficLightScheduleEntry extends Button {

    private static final int DEFAULT_EDIT_BOX_HEIGHT = 16;
    private static final int DEFAULT_ENTRY_HEIGHT = 22;
    public static final int HEIGHT = NewTrafficLightScheduleEditor.ENTRY_PADDING * 2 + DEFAULT_ENTRY_HEIGHT * 2;

    private final WidgetsCollection widgets = new WidgetsCollection();

    private final EditBox delayBox;
    private final ResizableButton addTimeButton;
    private final ResizableButton removeTimeButton;

    private final EditBox phaseIdBox;

    private TrafficLightAnimationData entry = new TrafficLightAnimationData();

    public NewTrafficLightScheduleEntry(int pX, int pY, int pWidth, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, HEIGHT, pMessage, pOnPress);

        Minecraft minecraft = Minecraft.getInstance();

        delayBox = GuiUtils.createEditBox(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 1,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1,
            38,
            DEFAULT_EDIT_BOX_HEIGHT - 2,
            minecraft.font,
            String.valueOf(entry.getDurationTicks()),
            true,
            (text) -> {
                try {
                    entry.setDurationSeconds(Integer.parseInt(text));
                } catch (Exception e) {}
            },
            (box, focus) -> {}
        );
        delayBox.setFilter(GuiUtils::editBoxNumberFilter);

        removeTimeButton = GuiUtils.createButton(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            GuiUtils.text("-"),
            (btn) -> {

            }
        );

        addTimeButton = GuiUtils.createButton(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH + 16 + 40,
            NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2,
            DEFAULT_EDIT_BOX_HEIGHT,
            DEFAULT_EDIT_BOX_HEIGHT,
            GuiUtils.text("+"),
            (btn) -> {

            }
        );

        phaseIdBox = GuiUtils.createEditBox(
            pX + NewTrafficLightScheduleEditor.ENTRY_PADDING + NewTrafficLightScheduleEditor.ENTRY_TIMELINE_COLUMN_WIDTH,
            (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + NewTrafficLightScheduleEditor.DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2),
            38,
            DEFAULT_EDIT_BOX_HEIGHT - 2,
            minecraft.font,
            String.valueOf(entry.getDurationTicks()),
            true,
            (text) -> {
                try {
                    entry.setDurationSeconds(Integer.parseInt(text));
                } catch (Exception e) {}
            },
            (box, focus) -> {}
        );
        phaseIdBox.setFilter(GuiUtils::editBoxNumberFilter);

        widgets.add(delayBox);
        widgets.add(addTimeButton);
        widgets.add(removeTimeButton);
        widgets.add(phaseIdBox);
    }

    public void setY(int y) {
        delayBox.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1;
        addTimeButton.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;
        removeTimeButton.y = y + NewTrafficLightScheduleEditor.ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - DEFAULT_EDIT_BOX_HEIGHT / 2;

        phaseIdBox.y = y + (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT * 1.5f - DEFAULT_EDIT_BOX_HEIGHT / 2 + 1);

        this.y = y;
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
            100,
            DEFAULT_ENTRY_HEIGHT,
            AreaStyle.GRAY,
            ButtonState.BUTTON
        );

        DynamicGuiRenderer.renderArea(
            pPoseStack,
            x + NewTrafficLightScheduleEditor.ENTRY_PADDING,
            y + (int)(NewTrafficLightScheduleEditor.ENTRY_PADDING * 1.5f + DEFAULT_ENTRY_HEIGHT),
            100,
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
            9,
            9,
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
            0,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
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
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            0,
            NewTrafficLightScheduleEditor.TIMELINE_UW,
            NewTrafficLightScheduleEditor.TIMELINE_VH,
            NewTrafficLightScheduleEditor.TEXTURE_WIDTH,
            NewTrafficLightScheduleEditor.TEXTURE_HEIGHT
        );

        if (isMouseOver(pMouseX, pMouseY)) {
            fill(pPoseStack, x + 1, y, x + 1 + width, y + height, 0x22FFFFFF);
        }

        widgets.performForEach(x -> x.visible, x -> x.render(pPoseStack, pMouseX, pMouseY, pPartialTick));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        widgets.performForEach(x -> x.visible, x -> x.mouseClicked(pMouseX, pMouseY, pButton));
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
