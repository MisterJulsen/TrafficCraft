package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
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
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.widgets.TrafficLightScheduleEntry;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class TrafficLightScheduleEditor extends CommonScreen {

    public static final ResourceLocation WIDGETS = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_schedule_icons.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

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
    private VerticalScrollBar scrollBar;

    private final Screen last;

    private final List<TrafficLightScheduleEntry> entries = new ArrayList<>();
    private final Map<Integer, TrafficLightType> phaseIdTypes = new HashMap<>();

    // settings
    private final BlockPos pos;
    private final Level level;
    private final boolean isController;
    private final TrafficLightSchedule schedule;

    //texts
    private static final Component textStart = Utils.translate("gui.trafficcraft.trafficlightschedule.start");
    private static final Component textEnd = Utils.translate("gui.trafficcraft.trafficlightschedule.end");
    private static final Component textAddEntry = Utils.translate("gui.trafficcraft.trafficlightschedule.add_entry");
    private static final String textLoop = Utils.translate("gui.trafficcraft.trafficlightschedule.loop").getString();

    protected TrafficLightScheduleEditor(Screen last, Level level, BlockPos pos) {
        super(Utils.translate("gui.trafficcraft.trafficlightschedule.title"));
        this.last = last;
        this.pos = pos;
        this.level = level;
        this.isController = isController();
        schedule = getSchedule().copy();

        if (isController()) {
            if (level.getBlockEntity(pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                blockEntity.getTrafficLightLocations().stream().filter(x -> 
                    level.isLoaded(x.getLocationBlockPos()) &&
                    level.getBlockEntity(x.getLocationBlockPos()) instanceof TrafficLightBlockEntity
                ).map(x -> (TrafficLightBlockEntity)level.getBlockEntity(x.getLocationBlockPos())).forEach(x -> {
                    int phaseId = x.getPhaseId();
                    TrafficLightType type = x.getTLType();
                    if (phaseIdTypes.containsKey(phaseId)) {
                        TrafficLightType savedType = phaseIdTypes.get(phaseId);
                        if (savedType != null && savedType != type) {
                            phaseIdTypes.remove(phaseId);
                            phaseIdTypes.put(phaseId, type);
                        }
                    } else {
                        phaseIdTypes.put(phaseId, type);
                    }
                });
            }
        } else {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof TrafficLightBlockEntity blockEntity) {
                phaseIdTypes.put(0, blockEntity.getTLType());
            }
        }
    }

    private boolean isController() {
        return level.getBlockEntity(pos) instanceof TrafficLightControllerBlockEntity;
    }

    private TrafficLightSchedule getSchedule() {
        if (isController && level.getBlockEntity(pos) instanceof TrafficLightControllerBlockEntity blockEntity) {
            return blockEntity.getFirstOrMainSchedule();
        } else if (level.getBlockEntity(pos) instanceof TrafficLightBlockEntity blockEntity) {
            return blockEntity.getSchedule();
        }

        return new TrafficLightSchedule();
    }

    public Map<Integer, TrafficLightType> getPhaseTypes() {
        return phaseIdTypes;
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
    protected void onDone() {
        super.onDone();
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new TrafficLightSchedulePacket(
            pos,
            List.of(schedule)
        ));
        onClose();
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
            schedule.getTrigger().getIconStack(),
            areaHeader.getLeft() + 1,
            areaHeader.getTop() + 1,
            headerW,                
            areaHeader.getHeight() - 2,
            Utils.translate(schedule.getTrigger().getValueTranslationKey(ModMain.MOD_ID)),
            (btn) -> {
                ItemButton ibtn = (ItemButton)btn;
                schedule.setTrigger(schedule.getTrigger().next());
                ibtn.withItem(schedule.getTrigger().getIconStack());
                btn.setMessage(Utils.translate(schedule.getTrigger().getValueTranslationKey(ModMain.MOD_ID)));
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
            Utils.text(textLoop + ": " + (schedule.isLoop() ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())),
            (btn) -> {
                schedule.setLoop(!schedule.isLoop());
                btn.setMessage(Utils.text(textLoop + ": " + (schedule.isLoop() ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())));
            }
        ));

        // add entry btn
        addButton(
            guiLeft + PADDING,
            guiTop + WINDOW_HEIGHT - PADDING - 20,
            20,
            20,
            Utils.text("+"),
            (btn) -> {
                createNewEntry();
            },
            Tooltip.of(textAddEntry)
        );

        addButton(
            guiLeft + WINDOW_WIDTH - PADDING - 90,
            guiTop + WINDOW_HEIGHT - PADDING - 20,
            90,
            20,
            CommonComponents.GUI_CANCEL,
            (btn) -> {
                onClose();
            },
            null
        );

        addButton(
            guiLeft + WINDOW_WIDTH - PADDING - 180 - 4,
            guiTop + WINDOW_HEIGHT - PADDING - 20,
            90,
            20,
            CommonComponents.GUI_DONE,
            (btn) -> {
                onDone();
            },
            null
        );

        // scrollbar
        scrollBar = addRenderableWidget(new VerticalScrollBar(areaWorkspace.getRight(), areaWorkspace.getTop(), areaWorkspace.getHeight(), areaWorkspace));
        scrollBar.setWidth(8);
        scrollBar.setStepSize(12);
        scrollBar.setAutoScrollerHeight(true);

        initEntryWidgets();
    }

    private void updateScrollBar() {
        scrollBar.setMaxRowsOnPage(areaWorkspace.getHeight() - 2);
        scrollBar.updateMaxScroll(entries.stream().mapToInt(x -> x.getHeight()).sum() + 2 * (DEFAULT_ENTRY_HEIGHT + ENTRY_PADDING));
    }

    private void removeEntry(TrafficLightScheduleEntryData entry) {
        schedule.getEntries().removeIf(x -> x == entry);

        initEntryWidgets();
    }

    private void move(TrafficLightScheduleEntryData entry, int offset) {
        int index = -1;
        List<TrafficLightScheduleEntryData> entries = schedule.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == entry) {
                index = i;
                break;
            }
        }
        int newIndex = index + offset;

        if (newIndex < 0 || newIndex >= schedule.getEntries().size()) {
            return;
        }

        TrafficLightScheduleEntryData data = schedule.getEntries().remove(index);
        this.schedule.getEntries().add(newIndex, data);

        initEntryWidgets();
    }

    private void createNewEntry() {
        schedule.getEntries().add(new TrafficLightScheduleEntryData());
        initEntryWidgets();
    }

    private void initEntryWidgets() {
        entries.clear();

        schedule.getEntries().forEach(x -> {
            entries.add(new TrafficLightScheduleEntry(this, !isController, x, areaWorkspace.getLeft(), 0, areaWorkspace.getWidth() - 2,
                (entry) -> {
                    removeEntry(entry);
                },
                (entry, offset) -> {
                    move(entry, offset);
                }
            ));
        });
        
        updateScrollBar();
    }

    @Override
    public void renderBg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        DynamicGuiRenderer.renderWindow(pPoseStack, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        DynamicGuiRenderer.renderArea(pPoseStack, areaHeader, AreaStyle.GRAY, ButtonState.SUNKEN);
        DynamicGuiRenderer.renderContainerBackground(pPoseStack, areaWorkspace);
        font.draw(pPoseStack, title, width / 2 - font.width(title) / 2, guiTop + 7, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
    
        GuiUtils.blit(WIDGETS, pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TIMELINE_UW / 2, areaWorkspace.getTop() + 1, TIMELINE_UW, areaWorkspace.getHeight() - 2, 27, 20, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        
        GuiUtils.swapAndBlitColor(minecraft.getMainRenderTarget(), GuiUtils.getFramebuffer());
        GuiUtils.startStencil(pPoseStack, areaWorkspace.getLeft() + 1, areaWorkspace.getTop() + 1, areaWorkspace.getWidth() - 2, areaWorkspace.getHeight() - 2);
        pPoseStack.pushPose();
        pPoseStack.translate(0, -scrollBar.getScrollValue(), 0);

        int y = areaWorkspace.getTop() + 1;
        y = renderInfo(pPoseStack, y, textStart);

        int offset = scrollBar.getScrollValue();
        for (TrafficLightScheduleEntry entry : entries) {
            entry.setY(y);
            y += TrafficLightScheduleEntry.HEIGHT;
            if (y > areaWorkspace.getTop() + offset && y - TrafficLightScheduleEntry.HEIGHT < areaWorkspace.getTop() + areaWorkspace.getHeight() + offset) {                
                entry.render(pPoseStack, areaWorkspace.isInBounds(pMouseX, pMouseY) ? pMouseX : -1, areaWorkspace.isInBounds(pMouseX, pMouseY) ? pMouseY + scrollBar.getScrollValue() : -1, pPartialTick);
            }
        }
        
        y = renderInfo(pPoseStack, y, textEnd);

        pPoseStack.popPose();
        GuiUtils.endStencil();        
        net.minecraftforge.client.gui.GuiUtils.drawGradientRect(pPoseStack.last().pose(), 200, areaWorkspace.getLeft() + 1, areaWorkspace.getTop() + 1, areaWorkspace.getRight() - 1, areaWorkspace.getTop() + 10, 0x77000000, 0x00000000);
        net.minecraftforge.client.gui.GuiUtils.drawGradientRect(pPoseStack.last().pose(), 200, areaWorkspace.getLeft() + 1, areaWorkspace.getBottom() - 10, areaWorkspace.getRight() - 1, areaWorkspace.getBottom() - 1, 0x00000000, 0x77000000);
        GuiUtils.swapAndBlitColor(GuiUtils.getFramebuffer(), minecraft.getMainRenderTarget());        

        super.renderBg(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderFg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderFg(pPoseStack, pMouseX, pMouseY, pPartialTick);
        int offset = scrollBar.getScrollValue();
        
        if (areaWorkspace.isInBounds(pMouseX, pMouseY)) {
            for (TrafficLightScheduleEntry entry : entries) {
                entry.renderTooltips(pPoseStack, pMouseX, pMouseY, offset);
            }
        }
    }

    public int renderInfo(PoseStack pPoseStack, int y, Component text) {
        DynamicGuiRenderer.renderArea(pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING, y + ENTRY_PADDING / 2, Math.min(ENTRY_TIMELINE_COLUMN_WIDTH + font.width(text) + 8, areaWorkspace.getWidth() - ENTRY_PADDING * 2), DEFAULT_ENTRY_HEIGHT, AreaStyle.GRAY, ButtonState.BUTTON);
        font.draw(pPoseStack, text, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH, y + ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - font.lineHeight / 2, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
        GuiUtils.blit(WIDGETS, pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TIMELINE_UW / 2, y, TIMELINE_UW, DEFAULT_ENTRY_HEIGHT + ENTRY_PADDING, 27, 20, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        GuiUtils.blit(WIDGETS, pPoseStack, areaWorkspace.getLeft() + ENTRY_PADDING + ENTRY_TIMELINE_COLUMN_WIDTH / 2 - TIMELINE_UW / 2, y + ENTRY_PADDING / 2 + DEFAULT_ENTRY_HEIGHT / 2 - TIMELINE_VH / 2, TIMELINE_UW, TIMELINE_VH, 0, 20, TIMELINE_UW, TIMELINE_VH, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        
        return y + DEFAULT_ENTRY_HEIGHT + ENTRY_PADDING;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {        
        
        if ((this.shouldCloseOnEsc() && pKeyCode == InputConstants.KEY_ESCAPE) || (!(getFocused() instanceof EditBox) && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode)))) {
            this.onClose();
            return true;
        }
        
        if (entries.stream().anyMatch(x -> x.keyPressed(pKeyCode, pScanCode, pModifiers))) {
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (entries.stream().anyMatch(x -> x.charTyped(pCodePoint, pModifiers))) {
            return true;
        }

        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		float scrollOffset = scrollBar.getScrollValue();

        if (areaWorkspace.isInBounds(pMouseX, pMouseY)) {
            for (AbstractWidget w : entries) {
                if (w.mouseClicked(pMouseX, pMouseY + scrollOffset, pButton)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		float scrollOffset = scrollBar.getScrollValue();

        if (entries.stream().anyMatch(x -> x.mouseScrolled(pMouseX, pMouseY + scrollOffset, pDelta))) {
            return true;
        }

		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
}
