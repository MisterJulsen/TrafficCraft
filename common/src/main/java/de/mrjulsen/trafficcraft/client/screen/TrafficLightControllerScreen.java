package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.Clipboard;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.ModGuiIcons;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficLightControllerScreen extends DLWindow {
    public static final Component title = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.title");
    

    private BlockPos blockPos;
    private Level level;
    
    // Settings
    private boolean status;
    private DLButton pasteButton;

    // Controls
    protected DLCycleButton<Boolean> statusButton;
    protected DLButton editScheduleButton;

    private Component textStatus = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.status");
    private Component textEditSchedule = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.edit_schedule");

    public TrafficLightControllerScreen(DLWindowManager manager, BlockPos pos, Level level) {
        super(manager);
        this.level = level;
        this.blockPos = pos;

        if (this.level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
            this.status = blockEntity.isRunning();
        }
        
        setSize(250, 100);
        windowSpawnPosition.set(WindowPosition.CENTER);

        DLPanel contentPanel = addComponent(new DLPanel(0, 40, width(), height()));
        contentPanel.anchor.set(EAlign.values());

        
        DLPanel schedulePanel = contentPanel.addComponent(new DLPanel(0, 0, 0, 20));
        FlowLayout statusLayout = new FlowLayout();
        statusLayout.flowDirection.set(Direction.HORIZONTAL);
        statusLayout.wrap.set(false);
        schedulePanel.layout.set(statusLayout);

        editScheduleButton = schedulePanel.addComponent(new DLButton(0, 0, 0, 20));        
        editScheduleButton.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        editScheduleButton.text.set(textEditSchedule);
        editScheduleButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new TrafficLightScheduleEditor(mgr, level, pos));
            return false;
        });
        
        // copy
        DLButton copyBtn = schedulePanel.addComponent(new DLButton(0, 0, 20, 20));
        copyBtn.text.set(TextUtils.EMPTY);
        copyBtn.icon.set(ModGuiIcons.COPY.getAsSprite(16, 16));
        copyBtn.layoutContraint.set(FlowLayout.FlowConstraint.END);
        copyBtn.tooltip.set(new DLTooltip(List.of(Constants.textCopy), 200));
        copyBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            if (level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                Clipboard.put(TrafficLightSchedule.class, blockEntity.getFirstOrMainSchedule());
            }
            return false;
        });
        
        DLButton pasteButton = schedulePanel.addComponent(new DLButton(0, 0, 20, 20));
        pasteButton.text.set(TextUtils.EMPTY);
        pasteButton.icon.set(ModGuiIcons.PASTE.getAsSprite(16, 16));
        pasteButton.layoutContraint.set(FlowLayout.FlowConstraint.END);
        pasteButton.tooltip.set(new DLTooltip(List.of(Constants.textPaste), 200));
        pasteButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            Optional<TrafficLightSchedule> schedule = Clipboard.get(TrafficLightSchedule.class);
            if (schedule.isPresent()) {
                ModNetworkManager.UPDATE_TRAFFIC_LIGHT_SCHEDULE.send(NetworkDirection.toServer(), new TrafficLightSchedulePacket(blockPos, List.of(schedule.get())));
            }
            return false;
        });
        

        statusButton = contentPanel.addComponent(new DLCycleButton<>(0, 0, 0, 20));
        statusButton.text.set(textStatus);
        statusButton.selectedItem.set(Optional.of(status));
        statusButton.cycling.set(true);
        statusButton.items.addAll(true, false);
        statusButton.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(c.selectedItem.get().map(b -> b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).orElse(CommonComponents.OPTION_OFF)));
        statusButton.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {            
            statusButton.selectedItem.get().ifPresent(v -> status = v);
            return false;
        });
        //pasteButton.set_active(false);
        
        contentPanel.addComponent(new DLPanel(0, 0, 0, 10));


        DLPanel closeBtns = contentPanel.addComponent(new DLPanel(0, 0, 0, 20));
        FlowLayout closeBtnsLayout = new FlowLayout();
        closeBtnsLayout.flowDirection.set(Direction.HORIZONTAL);
        closeBtnsLayout.horizontalGap.set(4);
        closeBtnsLayout.wrap.set(false);
        closeBtns.layout.set(closeBtnsLayout);

        DLButton doneBtn = closeBtns.addComponent(new DLButton(0, 0, 0, 20));
        doneBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        doneBtn.text.set(CommonComponents.GUI_DONE);
        doneBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onDone();
            return false;
        });        
        DLButton closeBtn = closeBtns.addComponent(new DLButton(0, 0, 0, 20));
        closeBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        closeBtn.text.set(CommonComponents.GUI_CANCEL);
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });


        
        contentPanel.addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            setHeight(40 + e.layoutResult().contentHeight());
            return false;
        });
        FlowLayout layout = new FlowLayout();
        layout.fillCrossAxis.set(true);
        layout.flowDirection.set(Direction.VERTICAL);
        layout.verticalGap.set(5);
        layout.wrap.set(false);
        contentPanel.layout.set(layout);
    }

    public TrafficLightControllerBlockEntity getBlockEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightControllerBlockEntity ? (TrafficLightControllerBlockEntity)be : null;
    }

    protected void onDone() {
        ModNetworkManager.UPDATE_TRAFFIC_LIGHT_CONTROLLER.send(NetworkDirection.toServer(), new TrafficLightControllerPacket(blockPos, status));
        getWindowManager().closeWindow(this);
    }

    @Override
    public void tick() {
        super.tick();
        //pasteButton.set_active(Clipboard.contains(TrafficLightSchedule.class));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 0, title, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.CENTER, true);
    }
}
