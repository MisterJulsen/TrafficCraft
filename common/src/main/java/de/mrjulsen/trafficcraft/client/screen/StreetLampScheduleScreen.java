package de.mrjulsen.trafficcraft.client.screen;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.*;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
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

        double ticksPerDay = VanillaTimeSystem.INSTANCE.getTicksPerDay();
        double daytimeOffset = VanillaTimeSystem.INSTANCE.getDaytimeOffset();
        double timeSteps = ticksPerDay / (24 * 4);

        DLPanel contentPanel = addComponent(new DLPanel(0, 40, width(), height()));
        contentPanel.anchor.set(EAlign.values());

        timeFormatButton = contentPanel.addComponent(new DLCycleButton<>(0, 0, 0, 20));
        timeFormatButton.text.set(textTimeFormat);
        timeFormatButton.cycling.set(true);
        timeFormatButton.items.addAll(ETimeFormat.values());
        timeFormatButton.selectedItem.set(Optional.of(timeFormat));
        timeFormatButton.textFormat.set(f -> TextUtils.empty().append(f.text.get()).append(": ").append(f.selectedItem.get().map(ITranslatableEnum::getValueTranslation).orElse(TextUtils.empty())));

        timeOnSlider = contentPanel.addComponent(new DLSlider(0, 0, 0, 20));
        timeOnSlider.text.set(textTurnOnTime);
        timeOnSlider.min.set(0D);
        timeOnSlider.max.set(ticksPerDay - timeSteps);
        timeOnSlider.step.set(timeSteps);
        timeOnSlider.value.set((turnOnTime + daytimeOffset) % ticksPerDay);
        timeOnSlider.textFormat.set((c) -> {
            int time = (int)positiveModulo(c.value.get() - daytimeOffset, ticksPerDay);
            String suffixKey = getTimeSuffix(time);
            return TextUtils.text(c.text.get().getString())
                    .append(": ")
                    .append(new DLTime(time, VanillaTimeSystem.INSTANCE).format(timeFormat.getFormat(), TimeContext.INGAME))
                    .append(suffixKey == null ? TextUtils.empty() : TextUtils.text(" (").append(TextUtils.translate(suffixKey)).append(")"));
        });
        timeOnSlider.addEventListener(DLSlider.ValueChangedEvent.class, (s, e) -> {
            this.turnOnTime = (int)positiveModulo(e.value() - daytimeOffset, ticksPerDay);
            return false;
        });
        
        timeOffSlider = contentPanel.addComponent(new DLSlider(0, 0, 0, 20));
        timeOffSlider.text.set(textTurnOffTime);
        timeOffSlider.min.set(0D);
        timeOffSlider.max.set(ticksPerDay - timeSteps);
        timeOffSlider.step.set(timeSteps);
        timeOffSlider.value.set((turnOffTime + daytimeOffset) % ticksPerDay);
        timeOffSlider.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(new DLTime((long)positiveModulo(c.value.get() - daytimeOffset, ticksPerDay), VanillaTimeSystem.INSTANCE).format(timeFormat.getFormat(), TimeContext.INGAME)));
        timeOffSlider.addEventListener(DLSlider.ValueChangedEvent.class, (s, e) -> {
            this.turnOffTime = (int)positiveModulo(e.value() - daytimeOffset, ticksPerDay);
            return false;
        });

        timeFormatButton.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            timeFormatButton.selectedItem.get().ifPresent(c -> this.timeFormat = c);
            timeOnSlider.invokeEvent(timeOnSlider, new DLSlider.TextFormatChanged<>(timeOnSlider.textFormat.get()), true);
            timeOffSlider.invokeEvent(timeOffSlider, new DLSlider.TextFormatChanged<>(timeOffSlider.textFormat.get()), true);
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

    private double positiveModulo(double num, double mod) {
        return (num % mod + mod) % mod;
    }

    protected void onDone() {
        ModNetworkManager.UPDATE_STREET_LAMP_CONFIG_CARD.send(NetworkDirection.toServer(), new StreetLampConfigPacket(this.turnOnTime, this.turnOffTime, this.timeFormat));
        getWindowManager().closeWindow(this);
    }

    private String getTimeSuffix(int value) {
        long ticksPerDay = VanillaTimeSystem.INSTANCE.getTicksPerDay();
        value = (int)(value % ticksPerDay);
        return switch (value) {
            case 0 -> "gui.trafficcraft.daytime.midnight";
            case 6000 -> "gui.trafficcraft.daytime.morning";
            case 12000 -> "gui.trafficcraft.daytime.noon";
            case 18000 -> "gui.trafficcraft.daytime.evening";
            default -> null;
        };
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 0, title, DLColor.WHITE, ETextAlignment.CENTER, true);
    }
}
