package de.mrjulsen.trafficcraft.client.screen;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.TimeUtils;
import de.mrjulsen.mcdragonlib.util.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.network.packets.cts.StreetLampConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class StreetLampScheduleScreen extends DLScreen {

    public static final Component title = TextUtils.translate("gui.trafficcraft.streetlampconfig.title");
    
    private int guiTop = 50;
    
    private static final int LINES = 3;
    private static final int SPACING_Y = 25;
    private static final int HEIGHT = (int)((LINES + 2.5) * SPACING_Y);

    // Settings
    private int turnOnTime;
    private int turnOffTime;
    private TimeFormat timeFormat = TimeFormat.TICKS;

    // Controls
    protected DLSlider timeOnSlider;
    protected DLSlider timeOffSlider; 
    protected DLCycleButton<TimeFormat> timeFormatButton;

    private Component textTurnOnTime = TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_on_time");
    private Component textTurnOffTime = TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_off_time");
    private Component textTimeFormat = TextUtils.translate("gui.trafficcraft.streetlampconfig.time_format");

    public StreetLampScheduleScreen(int timeOn, int timeOff, TimeFormat format) {
        super(title);
        this.turnOnTime = timeOn;
        this.turnOffTime = timeOff;
        this.timeFormat = format;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        
        guiTop = this.height / 2 - HEIGHT / 2;

        addButton(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + 3, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        this.timeFormatButton = addCycleButton(DragonLib.MODID, TimeFormat.class, this.width / 2 - 100, guiTop + (int)(SPACING_Y * 1), 200, 20, textTimeFormat, timeFormat,
        (btn, value) -> {
            this.timeFormat = value;
        }, DLTooltip.of(DragonLib.MODID, TimeFormat.class));

        this.timeOnSlider = addSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 2), 200, 20, textTurnOnTime, TextUtils.text(""), 0, 23750, 250, turnOnTime, true,
        (slider, value) -> {            
            //slider.getMessage()(TextUtils.translate(getTimeSuffix(value.intValue())));
            this.turnOnTime = value.intValue();
        }, null, null);
        this.addRenderableWidget(timeOnSlider); 

        this.timeOffSlider = addSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 3), 200, 20, textTurnOffTime, TextUtils.text(""), 0, 23750, 250, turnOffTime, true,
        (slider, value) -> {
            //slider.setSuffix(TextUtils.translate(getTimeSuffix(value.intValue())));
            this.turnOffTime = value.intValue();
        }, null, null);
        this.addRenderableWidget(timeOffSlider); 
    }

    @Override
    protected void onDone() {
        TrafficCraft.net().sendToServer(new StreetLampConfigPacket(this.turnOnTime, this.turnOffTime, this.timeFormat));
        this.onClose();
    }

    private String getTimeSuffix(int value) {        
        value = value % (int)DragonLib.ticksPerDay();
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {        
        renderTransparentBackground(graphics);        
        graphics.drawCenteredString(this.font, getTitle(), this.width / 2, guiTop, 16777215);
        
        String timeOnSuffix = this.getTimeSuffix(this.timeOnSlider.getValueInt());
        this.timeOnSlider.setMessage(TextUtils.text(TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_on_time", TimeUtils.parseTime(this.timeOnSlider.getValueInt(), timeFormat)).getString() + (timeOnSuffix == null ? "" :  " (" + TextUtils.translate(timeOnSuffix).getString() + ")")));

        String timeOffSuffix = this.getTimeSuffix(this.timeOffSlider.getValueInt());
        this.timeOffSlider.setMessage(TextUtils.text(TextUtils.translate("gui.trafficcraft.streetlampconfig.turn_off_time", TimeUtils.parseTime(this.timeOffSlider.getValueInt(), timeFormat)).getString() + (timeOffSuffix == null ? "" :  " (" + TextUtils.translate(timeOffSuffix).getString() + ")")));

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
