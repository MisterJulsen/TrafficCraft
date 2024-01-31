package de.mrjulsen.trafficcraft.client.screen;

import de.mrjulsen.mcdragonlib.DragonLibConstants;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.DragonLibTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.TimeUtils;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.mcdragonlib.utils.TimeUtils.TimeFormat;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.StreetLampConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

@OnlyIn(Dist.CLIENT)
public class StreetLampScheduleScreen extends CommonScreen {

    public static final Component title = Utils.translate("gui.trafficcraft.streetlampconfig.title");
    
    private int guiTop = 50;
    
    private static final int LINES = 3;
    private static final int SPACING_Y = 25;
    private static final int HEIGHT = (int)((LINES + 2.5) * SPACING_Y);

    // Settings
    private int turnOnTime;
    private int turnOffTime;
    private TimeFormat timeFormat = TimeFormat.TICKS;

    // Controls
    protected ForgeSlider timeOnSlider;
    protected ForgeSlider timeOffSlider; 
    protected ResizableCycleButton<TimeFormat> timeFormatButton;

    private Component textTurnOnTime = Utils.translate("gui.trafficcraft.streetlampconfig.turn_on_time");
    private Component textTurnOffTime = Utils.translate("gui.trafficcraft.streetlampconfig.turn_off_time");
    private Component textTimeFormat = Utils.translate("gui.trafficcraft.streetlampconfig.time_format");

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


        /* Default page */

        addButton(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + 3, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        this.timeFormatButton = addCycleButton(DragonLibConstants.DRAGONLIB_MODID, TimeFormat.class, this.width / 2 - 100, guiTop + (int)(SPACING_Y * 1), 200, 20, textTimeFormat, timeFormat,
        (btn, value) -> {
            this.timeFormat = value;
        }, DragonLibTooltip.of(GuiUtils.getEnumTooltipData(DragonLibConstants.DRAGONLIB_MODID, this, TimeFormat.class, width / 4)));

        this.timeOnSlider = addSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 2), 200, 20, textTurnOnTime, Utils.text(""), 0, 23750, 250, turnOnTime, true,
        (slider, value) -> {            
            slider.setSuffix(Utils.translate(getTimeSuffix(value.intValue())));
            this.turnOnTime = value.intValue();
        }, null, null);
        this.addRenderableWidget(timeOnSlider); 

        this.timeOffSlider = addSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 3), 200, 20, textTurnOffTime, Utils.text(""), 0, 23750, 250, turnOffTime, true,
        (slider, value) -> {
            slider.setSuffix(Utils.translate(getTimeSuffix(value.intValue())));
            this.turnOffTime = value.intValue();
        }, null, null);
        this.addRenderableWidget(timeOffSlider); 
    }

    @Override
    protected void onDone() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new StreetLampConfigPacket(this.turnOnTime, this.turnOffTime, this.timeFormat));
        this.onClose();
    }

    private String getTimeSuffix(int value) {        
        value = value % DragonLibConstants.TICKS_PER_DAY;
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
        renderBackground(graphics);        
        graphics.drawCenteredString(this.font, getTitle(), this.width / 2, guiTop, 16777215);
        
        String timeOnSuffix = this.getTimeSuffix(this.timeOnSlider.getValueInt());
        this.timeOnSlider.setMessage(Utils.text(Utils.translate("gui.trafficcraft.streetlampconfig.turn_on_time", TimeUtils.parseTime(this.timeOnSlider.getValueInt(), timeFormat)).getString() + (timeOnSuffix == null ? "" :  " (" + Utils.translate(timeOnSuffix).getString() + ")")));

        String timeOffSuffix = this.getTimeSuffix(this.timeOffSlider.getValueInt());
        this.timeOffSlider.setMessage(Utils.text(Utils.translate("gui.trafficcraft.streetlampconfig.turn_off_time", TimeUtils.parseTime(this.timeOffSlider.getValueInt(), timeFormat)).getString() + (timeOffSuffix == null ? "" :  " (" + Utils.translate(timeOffSuffix).getString() + ")")));

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
