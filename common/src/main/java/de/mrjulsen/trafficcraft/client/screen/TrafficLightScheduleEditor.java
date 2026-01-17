package de.mrjulsen.trafficcraft.client.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.widgets.data.TrafficLightScheduleEditorWidget;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class TrafficLightScheduleEditor extends DLWindow {

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

    private DLPanel areaHeader;
    private TrafficLightScheduleEditorWidget container;

    private final Map<Integer, TrafficLightType> phaseIdTypes = new HashMap<>();

    // settings
    private final BlockPos pos;
    private final Level level;
    private final boolean isController;
    private final TrafficLightSchedule schedule;

    //texts
    private final Component title = TextUtils.translate("gui.trafficcraft.trafficlightschedule.title");
    private final Component textAddEntry = TextUtils.translate("gui.trafficcraft.trafficlightschedule.add_entry");
    private final Component textLoop = TextUtils.translate("gui.trafficcraft.trafficlightschedule.loop");

    public TrafficLightScheduleEditor(DLWindowManager manager, Level level, BlockPos pos) {
        super(manager);
        this.pos = pos;
        this.level = level;
        this.isController = isController();
        schedule = getSchedule().copy();

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

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

        areaHeader = addComponent(new DLPanel(PADDING, TOP_PADDING, width() - PADDING * 2, 22));
        FlowLayout headerLayout = new FlowLayout();
        headerLayout.padding.set(new Padding(1));
        headerLayout.wrap.set(false);
        headerLayout.flowDirection.set(Direction.HORIZONTAL);
        areaHeader.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                DLTextureSheet.DRAGONLIB_UI.getSprite("button_brown_down").render(e.graphics(), 0, 0, s.width(), s.height());
            }            
            return false;
        });

        DLCycleButton<TrafficLightTrigger> triggerBtn = areaHeader.addComponent(new DLCycleButton<>(0, 0, 1, 20));
        triggerBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        triggerBtn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        triggerBtn.drawFontShadow.set(false);
        triggerBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        triggerBtn.textFormat.set(c -> c.selectedItem.get().map(e -> e.getValueTranslation()).orElse(TextUtils.empty()));
        triggerBtn.icon.set(new DLSprite(schedule.getTrigger().getIconStack(), 16, false));
        triggerBtn.iconAlignment.set(ETextAlignment.LEFT);
        triggerBtn.textAlignment.set(ETextAlignment.LEFT);
        triggerBtn.items.addAll(TrafficLightTrigger.values());
        triggerBtn.selectedItem.set(Optional.of(schedule.getTrigger()));
        triggerBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            triggerBtn.selectedItem.get().ifPresent(c -> {
                schedule.setTrigger(c);
                triggerBtn.icon.set(new DLSprite(c.getIconStack(), 16, false));
            });
            return false;
        });

        DLCycleButton<Boolean> loopBtn = areaHeader.addComponent(new DLCycleButton<>(0, 0, 1, 20)); 
        loopBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        loopBtn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        loopBtn.drawFontShadow.set(false);
        loopBtn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
        loopBtn.text.set(textLoop);
        loopBtn.textFormat.set(c -> TextUtils.text(c.text.get().getString()).append(": ").append(c.selectedItem.get().map(b -> b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).orElse(CommonComponents.OPTION_OFF)));
        loopBtn.items.addAll(true, false);
        loopBtn.selectedItem.set(Optional.of(schedule.isLoop()));
        loopBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            loopBtn.selectedItem.get().ifPresent(c -> {
                schedule.setLoop(c);
            });
            return false;
        });
        areaHeader.layout.set(headerLayout);

        // add entry btn
        DLButton addBtn = addComponent(new DLButton(PADDING, height() - PADDING - 20, 20, 20));
        addBtn.text.set(TextUtils.text("+"));
        addBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            createNewEntry();
            container.refresh();
            return false;
        });
        addBtn.tooltip.set(new DLTooltip(List.of(textAddEntry), 200));


        
        DLButton cancelBtn = addComponent(new DLButton(width() - PADDING - 90, height() - PADDING - 20, 90, 20));
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });

        DLButton doneBtn = addComponent(new DLButton(width() - PADDING - 184, height() - PADDING - 20, 90, 20));
        doneBtn.text.set(CommonComponents.GUI_DONE);
        doneBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onDone();
            return false;
        });

        container = addComponent(new TrafficLightScheduleEditorWidget(PADDING, TOP_PADDING + areaHeader.height(), width() - PADDING * 2, height() - TOP_PADDING - areaHeader.height() - BOTTOM_PADDING, schedule, isController(), getPhaseTypes()));
    
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


    protected void onDone() {
        ModNetworkManager.UPDATE_TRAFFIC_LIGHT_SCHEDULE.send(NetworkDirection.toServer(), new TrafficLightSchedulePacket(pos, List.of(schedule)));
        getWindowManager().closeWindow(this);
    }

    private void createNewEntry() {
        schedule.getEntries().add(new TrafficLightScheduleEntryData());
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 7, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);     
    }
}
