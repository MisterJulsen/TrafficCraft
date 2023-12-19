package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.DragonLibConstants;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.Alignment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.VerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.client.widgets.NewTrafficLightScheduleEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class NewTrafficLightScheduleEditor extends CommonScreen {

    public static final ResourceLocation WIDGETS = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_schedule_icons.png");
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    public static final int WINDOW_WIDTH = 240;
    public static final int WINDOW_HEIGHT = 230;
    public static final int PADDING = 7;
    public static final int TOP_PADDING = 20;
    public static final int BOTTOM_PADDING = PADDING + 23;
    public static final int SCROLLBAR_WIDTH = 8;
    public static final int ENTRY_PADDING = 8;
    public static final int DEFAULT_ENTRY_HEIGHT = 18;
    public static final int TIMELINE_UW = 9;
    public static final int TIMELINE_VH = 9;
    public static final int ENTRY_TIMELINE_COLUMN_WIDTH = 20;
    
    private static final int HEADER_BUTTON_COUNT = 2; // trigger, loop

    private int guiLeft;
    private int guiTop;

    private GuiAreaDefinition areaHeader;
    private GuiAreaDefinition areaWorkspace;

    private final Screen last;

    private final List<NewTrafficLightScheduleEntry> entries = new ArrayList<>();

    // settings
    private TrafficLightTrigger trigger = TrafficLightTrigger.NONE;
    private boolean loop = true;

    //texts
    private static final Component textStart = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.start");
    private static final Component textEnd = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.end");
    private static final String textLoop = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.loop").getString();

    protected NewTrafficLightScheduleEditor(Screen last) {
        super(GuiUtils.translate("gui.trafficcraft.trafficlightschedule.title"));
        this.last = last;
    }

    @Override
    public void onClose() {
        if (last != null) {
            Minecraft.getInstance().setScreen(last);
            return;
        }   
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        entries.forEach(x -> x.tick());
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = width / 2 - WINDOW_WIDTH / 2;
        guiTop = height / 2 - WINDOW_HEIGHT / 2;

        entries.clear();

        areaHeader = new GuiAreaDefinition(guiLeft + PADDING, guiTop + TOP_PADDING, WINDOW_WIDTH - PADDING * 2, IconButton.DEFAULT_BUTTON_HEIGHT + 2);
        areaWorkspace = new GuiAreaDefinition(guiLeft + PADDING, guiTop + TOP_PADDING + areaHeader.getHeight(), WINDOW_WIDTH - PADDING * 2 - SCROLLBAR_WIDTH, WINDOW_HEIGHT - TOP_PADDING - areaHeader.getHeight() - BOTTOM_PADDING);

        int headerW = (areaHeader.getWidth() - 2) / HEADER_BUTTON_COUNT;
        // trigger
        final ItemButton bt = addRenderableWidget(new ItemButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            trigger.getIconStack(),
            areaHeader.getLeft() + 1,
            areaHeader.getTop() + 1,
            headerW,                
            areaHeader.getHeight() - 2,
            GuiUtils.translate(trigger.getValueTranslationKey(ModMain.MOD_ID)),
            (btn) -> {
                ItemButton ibtn = (ItemButton)btn;
                trigger = trigger.next();
                ibtn.withItem(trigger.getIconStack());
                btn.setMessage(GuiUtils.translate(trigger.getValueTranslationKey(ModMain.MOD_ID)));
            }
        ).withAlignment(Alignment.LEFT).withDefaultItemTooltip(false));

        addTooltip(Tooltip
            .of(GuiUtils.getEnumTooltipData(ModMain.MOD_ID, TrafficLightTrigger.class))
            .withMaxWidth(width / 4)
            .assignedTo(bt)
        );

        // loop
        addRenderableWidget(new IconButton(
            ButtonType.DEFAULT, 
            AreaStyle.BROWN, 
            Sprite.empty(),
            null,
            areaHeader.getLeft() + 1 + headerW,
            areaHeader.getTop() + 1,
            headerW,                
            areaHeader.getHeight() - 2,
            GuiUtils.text(textLoop + ": " + (loop ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())),
            (btn) -> {
                loop = !loop;
                btn.setMessage(GuiUtils.text(textLoop + ": " + (loop ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())));
            }
        ));

        // scrollbar
        VerticalScrollBar scrollBar = addRenderableWidget(new VerticalScrollBar(areaWorkspace.getRight(), areaWorkspace.getTop(), areaWorkspace.getHeight(), areaWorkspace));
        scrollBar.setWidth(8);

        entries.add(addWidget(new NewTrafficLightScheduleEntry(areaWorkspace.getLeft(), 0, areaWorkspace.getWidth() - 2, new TextComponent("textLoop"), null)));
        entries.add(addWidget(new NewTrafficLightScheduleEntry(areaWorkspace.getLeft(), 0, areaWorkspace.getWidth() - 2, new TextComponent("textLoop"), null)));
        entries.add(addWidget(new NewTrafficLightScheduleEntry(areaWorkspace.getLeft(), 0, areaWorkspace.getWidth() - 2, new TextComponent("textLoop"), null)));
    }

    @Override
    public void renderBg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        DynamicGuiRenderer.renderWindow(pPoseStack, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        DynamicGuiRenderer.renderArea(pPoseStack, areaHeader, AreaStyle.GRAY, ButtonState.SUNKEN);
        DynamicGuiRenderer.renderContainerBackground(pPoseStack, areaWorkspace);
        font.draw(pPoseStack, title, width / 2 - font.width(title) / 2, guiTop + 7, DragonLibConstants.DEFAULT_UI_FONT_COLOR);

        GuiUtils.swapAndBlitColor(minecraft.getMainRenderTarget(), GuiUtils.getFramebuffer());
        GuiUtils.startStencil(pPoseStack, areaWorkspace.getLeft() + 1, areaWorkspace.getTop() + 1, areaWorkspace.getWidth() - 2, areaWorkspace.getHeight() - 1);
        pPoseStack.pushPose();
        pPoseStack.translate(0, 0, 0);

        int y = areaWorkspace.getTop() + 1;
        y = renderInfo(pPoseStack, y, textStart);

        for (NewTrafficLightScheduleEntry entry : entries) {
            entry.setY(y);
            y += NewTrafficLightScheduleEntry.HEIGHT;
            entry.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        
        y = renderInfo(pPoseStack, y, textEnd);

        pPoseStack.popPose();
        GuiUtils.endStencil();        
        net.minecraftforge.client.gui.GuiUtils.drawGradientRect(pPoseStack.last().pose(), 200, areaWorkspace.getLeft() + 1, areaWorkspace.getTop() + 1, areaWorkspace.getRight() - 1, areaWorkspace.getTop() + 10, 0x77000000, 0x00000000);
        net.minecraftforge.client.gui.GuiUtils.drawGradientRect(pPoseStack.last().pose(), 200, areaWorkspace.getLeft() + 1, areaWorkspace.getBottom() - 10, areaWorkspace.getRight() - 1, areaWorkspace.getBottom() - 1, 0x00000000, 0x77000000);
        GuiUtils.swapAndBlitColor(GuiUtils.getFramebuffer(), minecraft.getMainRenderTarget());        

        super.renderBg(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public int renderInfo(PoseStack pPoseStack, int y, Component text) {
        DynamicGuiRenderer.renderArea(pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING, y + ENTRY_PADDING / 2, Math.min(ENTRY_TIMELINE_COLUMN_WIDTH + font.width(textStart), areaWorkspace.getWidth() - ENTRY_PADDING * 2), DEFAULT_ENTRY_HEIGHT, AreaStyle.GRAY, ButtonState.BUTTON);
        font.draw(pPoseStack, text, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH, y + ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - font.lineHeight / 2, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
        GuiUtils.blit(WIDGETS, pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TIMELINE_UW / 2, y, TIMELINE_UW, DEFAULT_ENTRY_HEIGHT + ENTRY_PADDING, 9, 9, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        GuiUtils.blit(WIDGETS, pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TIMELINE_UW / 2, y + ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - TIMELINE_VH / 2, TIMELINE_UW, TIMELINE_VH, 0, 0, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        
        return y + DEFAULT_ENTRY_HEIGHT + ENTRY_PADDING;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        entries.forEach(x -> x.keyPressed(pKeyCode, pScanCode, pModifiers));
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        entries.forEach(x -> x.charTyped(pCodePoint, pModifiers));
        return super.charTyped(pCodePoint, pModifiers);
    }
    
}
