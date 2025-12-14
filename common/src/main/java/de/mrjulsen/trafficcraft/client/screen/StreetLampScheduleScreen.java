package de.mrjulsen.trafficcraft.client.screen;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;
import de.mrjulsen.mcdragonlib.util.time.VanillaTimeSystem;
import de.mrjulsen.trafficcraft.network.packets.cts.StreetLampConfigPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import de.mrjulsen.trafficcraft.util.ETimeFormat;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class StreetLampScheduleScreen extends DLWindow {

    public static final Component title = TextUtils.translate("gui.trafficcraft.streetlampconfig.title");
    
    private int guiTop = 50;
    
    private static final int LINES = 3;
    private static final int SPACING_Y = 25;
    private static final int HEIGHT = (int)((LINES + 2.5) * SPACING_Y);

    // Settings
    private int turnOnTime;
    private int turnOffTime;
    private ETimeFormat timeFormat = ETimeFormat.TICKS;

    // Controls
    protected DLSlider timeOnSlider;
    protected DLSlider timeOffSlider; 
    protected DLCycleButton<ETimeFormat> timeFormatButton;

    private Component textTurnOnTime = TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_on_time");
    private Component textTurnOffTime = TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_off_time");
    private Component textTimeFormat = TextUtils.translate("gui.trafficcraft.streetlampconfig.time_format");

    public StreetLampScheduleScreen(DLWindowManager manager, int timeOn, int timeOff, ETimeFormat format) {
        super(manager);
        this.turnOnTime = timeOn;
        this.turnOffTime = timeOff;
        this.timeFormat = format;
        
        setSize(200, 100);
        windowSpawnPosition.set(WindowPosition.CENTER);

        DLPanel contentPanel = addComponent(new DLPanel(0, 40, width(), height()));
        contentPanel.anchor.set(EAlign.values());

        timeFormatButton = contentPanel.addComponent(new DLCycleButton<>(0, 0, 0, 20));
        timeFormatButton.text.set(textTimeFormat);
        timeFormatButton.cycling.set(true);
        timeFormatButton.items.addAll(ETimeFormat.values());
        timeFormatButton.selectedItem.set(Optional.of(timeFormat));
        timeFormatButton.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            timeFormatButton.selectedItem.get().ifPresent(c -> this.timeFormat = c);
            return false;
        });
        //timeFormatButton.tooltip.set(new DLTooltip(GuiUtils.getEnumTooltipData(ETimeFormat.class, 200), 200));

        timeOnSlider = contentPanel.addComponent(new DLSlider(0, 0, 0, 20));
        timeOnSlider.text.set(textTurnOnTime);
        timeOnSlider.min.set(0D);
        timeOnSlider.max.set(23750D);
        timeOnSlider.step.set(250D);
        timeOnSlider.value.set((double)turnOnTime);
        timeOnSlider.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(DLTime.fromTicks(c.value.get() - 6000, VanillaTimeSystem.INSTANCE).format(timeFormat.getFormat(), TimeContext.INGAME)));
        timeOnSlider.addEventListener(DLSlider.ValueChangedEvent.class, (s, e) -> {
            this.turnOnTime = (int)e.value();
            return false;
        });
        
        timeOffSlider = contentPanel.addComponent(new DLSlider(0, 0, 0, 20));
        timeOffSlider.text.set(textTurnOffTime);
        timeOffSlider.min.set(0D);
        timeOffSlider.max.set(23750D);
        timeOffSlider.step.set(250D);
        timeOffSlider.value.set((double)turnOffTime);
        timeOffSlider.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(DLTime.fromTicks(c.value.get() - 6000, VanillaTimeSystem.INSTANCE).format(timeFormat.getFormat(), TimeContext.INGAME)));
        timeOffSlider.addEventListener(DLSlider.ValueChangedEvent.class, (s, e) -> {
            this.turnOffTime = (int)e.value();
            return false;
        });
        
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
        DLButton cancelBtn = closeBtns.addComponent(new DLButton(0, 0, 0, 20));
        cancelBtn.layoutContraint.set(FlowLayout.FlowConstraint.FILL);
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
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

    protected void onDone() {
        ModNetworkManager.UPDATE_STREET_LAMP_CONFIG_CARD.send(NetworkDirection.toServer(), new StreetLampConfigPacket(this.turnOnTime, this.turnOffTime, this.timeFormat));
        getWindowManager().closeWindow(this);
    }

    private String getTimeSuffix(int value) {        
        value = value % 24000;
        switch (value) {
            case 0:
                return "gui.trafficcraft.daytime.midnight";                
            case 6000:
                return "gui.trafficcraft.daytime.morning";                
            case 12000:
                return "gui.trafficcraft.daytime.noon";                
            case 18000:
                return "gui.trafficcraft.daytime.evening";
            default:
                return null;
        }
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 0, title, DLColor.WHITE, ETextAlignment.CENTER, true);
    }
}
