package de.mrjulsen.trafficcraft.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.properties.TimeFormat;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.StreetLampConfigPacket;
import de.mrjulsen.trafficcraft.util.TimeUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

@OnlyIn(Dist.CLIENT)
public class StreetLampScheduleScreen extends Screen
{
    public static final Component title = new TextComponent("streetlampconfig");
    
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
    protected CycleButton<TimeFormat> timeFormatButton;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.streetlampconfig.title");
    private TranslatableComponent textTurnOnTime = new TranslatableComponent("gui.trafficcraft.streetlampconfig.turn_on_time");
    private TranslatableComponent textTurnOffTime = new TranslatableComponent("gui.trafficcraft.streetlampconfig.turn_off_time");
    private TranslatableComponent textTimeFormat = new TranslatableComponent("gui.trafficcraft.streetlampconfig.time_format");

    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    public StreetLampScheduleScreen(int timeOn, int timeOff, TimeFormat format)
    {
        super(title);
        this.turnOnTime = timeOn;
        this.turnOffTime = timeOff;
        this.timeFormat = format;
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void init()
    {
        super.init();
        
        guiTop = this.height / 2 - HEIGHT / 2;


        /* Default page */

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, btnDoneTxt, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 3, guiTop + (int)(SPACING_Y * 4.5f), 97, 20, btnCancelTxt, (p) -> {
            this.onClose();
        }));

        this.timeFormatButton = this.addRenderableWidget(CycleButton.<TimeFormat>builder((p) -> {            
            return new TranslatableComponent(p.getTranslationKey());
            })
                .withValues(TimeFormat.values()).withInitialValue(timeFormat)
                .create(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 1), 200, 20, textTimeFormat, (pCycleButton, pValue) -> {
                    this.timeFormat = pValue;
        }));

        this.timeOnSlider = new ForgeSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 2), 200, 20, textTurnOnTime, new TextComponent(""), 0, 23750, turnOnTime, 250, 1, true);
        this.addRenderableWidget(timeOnSlider); 

        this.timeOffSlider = new ForgeSlider(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 3), 200, 20, textTurnOffTime, new TextComponent(""), 0, 23750, turnOffTime, 250, 1, true);
        this.addRenderableWidget(timeOffSlider); 
    }

    private void onDone() {
        this.turnOnTime = this.timeOnSlider.getValueInt();
        this.turnOffTime = this.timeOffSlider.getValueInt();
        NetworkManager.MOD_CHANNEL.sendToServer(new StreetLampConfigPacket(this.turnOnTime, this.turnOffTime, this.timeFormat));
        this.onClose();
    }

    private String getTimeSuffix(int value) {        
        value = value % Constants.TICKS_PER_DAY;
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
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {        
        renderBackground(stack, 0);        
        drawCenteredString(stack, this.font, textTitle, this.width / 2, guiTop, 16777215);
        
        String timeOnSuffix = this.getTimeSuffix(this.timeOnSlider.getValueInt());
        this.timeOnSlider.setMessage(new TextComponent(new TranslatableComponent("gui.trafficcraft.streetlampconfig.turn_on_time", TimeUtils.parseTime(this.timeOnSlider.getValueInt(), timeFormat)).getString() + (timeOnSuffix == null ? "" :  " (" + new TranslatableComponent(timeOnSuffix).getString() + ")")));

        String timeOffSuffix = this.getTimeSuffix(this.timeOffSlider.getValueInt());
        this.timeOffSlider.setMessage(new TextComponent(new TranslatableComponent("gui.trafficcraft.streetlampconfig.turn_off_time", TimeUtils.parseTime(this.timeOffSlider.getValueInt(), timeFormat)).getString() + (timeOffSuffix == null ? "" :  " (" + new TranslatableComponent(timeOffSuffix).getString() + ")")));

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_)))
        {
            this.onClose();
            return true;
        }
        else
        {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
