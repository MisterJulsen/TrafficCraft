package de.mrjulsen.trafficcraft.client.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.widgets.TrafficLightScheduleContainer;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class TrafficLightScheduleEditor extends DLScreen {

    public static final ResourceLocation WIDGETS = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/traffic_light_schedule_icons.png");
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

    private TrafficLightScheduleContainer container;

    private final Screen last;

    private final Map<Integer, TrafficLightType> phaseIdTypes = new HashMap<>();

    // settings
    private final BlockPos pos;
    private final Level level;
    private final boolean isController;
    private final TrafficLightSchedule schedule;

    //texts
    private static final Component textAddEntry = TextUtils.translate("gui.trafficcraft.trafficlightschedule.add_entry");
    private static final String textLoop = TextUtils.translate("gui.trafficcraft.trafficlightschedule.loop").getString();

    protected TrafficLightScheduleEditor(Screen last, Level level, BlockPos pos) {
        super(TextUtils.translate("gui.trafficcraft.trafficlightschedule.title"));
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
        TrafficCraft.net().sendToServer(new TrafficLightSchedulePacket(
            pos,
            List.of(schedule)
        ));
        onClose();
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = width / 2 - WINDOW_WIDTH / 2;
        guiTop = height / 2 - WINDOW_HEIGHT / 2;

        areaHeader = new GuiAreaDefinition(guiLeft + PADDING, guiTop + TOP_PADDING, WINDOW_WIDTH - PADDING * 2, DLIconButton.DEFAULT_BUTTON_HEIGHT + 2);
        areaWorkspace = new GuiAreaDefinition(guiLeft + PADDING, guiTop + TOP_PADDING + areaHeader.getHeight(), WINDOW_WIDTH - PADDING * 2, WINDOW_HEIGHT - TOP_PADDING - areaHeader.getHeight() - BOTTOM_PADDING);

        int headerW = (areaHeader.getWidth() - 2) / HEADER_BUTTON_COUNT;
        // trigger
        final DLItemButton bt = addRenderableWidget(new DLItemButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            schedule.getTrigger().getIconStack(),
            areaHeader.getLeft() + 1,
            areaHeader.getTop() + 1,
            headerW,                
            areaHeader.getHeight() - 2,
            TextUtils.translate(schedule.getTrigger().getValueTranslationKey(TrafficCraft.MOD_ID)),
            (btn) -> {
                DLItemButton ibtn = (DLItemButton)btn;
                schedule.setTrigger(schedule.getTrigger().next());
                ibtn.withItem(schedule.getTrigger().getIconStack());
                btn.setMessage(TextUtils.translate(schedule.getTrigger().getValueTranslationKey(TrafficCraft.MOD_ID)));
            }
        ).withAlignment(EAlignment.LEFT).withDefaultItemTooltip(false));

        addTooltip(DLTooltip
            .of(TrafficCraft.MOD_ID, TrafficLightTrigger.class)
            .withMaxWidth(width / 4)
            .assignedTo(bt)
        );

        // loop
        addRenderableWidget(new DLIconButton(
            ButtonType.DEFAULT, 
            AreaStyle.BROWN, 
            Sprite.empty(),
            null,
            areaHeader.getLeft() + 1 + headerW,
            areaHeader.getTop() + 1,
            headerW,                
            areaHeader.getHeight() - 2,
            TextUtils.text(textLoop + ": " + (schedule.isLoop() ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())),
            (btn) -> {
                schedule.setLoop(!schedule.isLoop());
                btn.setMessage(TextUtils.text(textLoop + ": " + (schedule.isLoop() ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())));
            }
        ));

        // add entry btn
        addButton(
            guiLeft + PADDING,
            guiTop + WINDOW_HEIGHT - PADDING - 20,
            20,
            20,
            TextUtils.text("+"),
            (btn) -> {
                createNewEntry();
            },
            DLTooltip.of(textAddEntry)
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

        container = addRenderableWidget(new TrafficLightScheduleContainer(schedule, isController(), getPhaseTypes(), areaWorkspace.getX(), areaWorkspace.getY(), areaWorkspace.getWidth(), areaWorkspace.getHeight()));
    }

    private void createNewEntry() {
        schedule.getEntries().add(new TrafficLightScheduleEntryData());
        container.init();
    }

    /*
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
        container.init();
    }
        */

        /*
    private void initEntryWidgets() {

        schedule.getEntries().forEach(x -> {
            entries.add(addRenderableWidget(new TrafficLightScheduleEntry(getPhaseTypes(), !isController, x, areaWorkspace.getLeft(), 0, areaWorkspace.getWidth() - 2,
                (entry) -> {
                    removeEntry(entry);
                },
                (entry, offset) -> {
                    move(entry, offset);
                }
            )));
        });
    }
        */

    @Override
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderScreenBackground(graphics);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT);
        DynamicGuiRenderer.renderArea(graphics, areaHeader, AreaStyle.GRAY, ButtonState.DISABLED);
        GuiUtils.drawString(graphics, font, width / 2, guiTop + 7, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.CENTER, false);        
        super.renderMainLayer(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
