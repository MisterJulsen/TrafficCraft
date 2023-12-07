package de.mrjulsen.trafficcraft.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.client.screen.TrafficLightScheduleScreen;
import de.mrjulsen.trafficcraft.client.widgets.data.WidgetData;
import de.mrjulsen.trafficcraft.data.TrafficLightAnimationData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TrafficLightScheduleEntry {
    
    private TrafficLightScheduleScreen parent;

    private static final int MODE_BTN_X_OFFSET = 116;
    private static final int MODE_BTN_Y_OFFSET = 4;
    private static final int MODE_BTN_W = 85;
    private static final int MODE_BTN_H = 16;
    
    private TrafficLightAnimationData data;
    
    protected EditBox idInput;
    protected EditBox timeInput;
    protected Button timeAddBtn;
    protected Button timeRemoveBtn;
    protected CycleButton<TrafficLightMode> modeButton;

    private boolean idEditable;

    public int x, y, width, height;

    public boolean selected;

    public List<WidgetData> renderableWidgets = new ArrayList<WidgetData>();

    private TranslatableComponent textMode = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.mode");

    private TranslatableComponent tooltipAdd = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.time_add");
    private TranslatableComponent tooltipRemove = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.time_remove");
    
    private static final ResourceLocation WIDGETS = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_schedule_widgets.png");

    public TrafficLightScheduleEntry(TrafficLightScheduleScreen parent, int x, int y, int width, int height, boolean idEditable, TrafficLightAnimationData data) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.data = data;
        this.idEditable = idEditable;
        this.parent = parent;
    }

    public void tick() {
        this.idInput.tick();
        this.timeInput.tick();
    }

    public TrafficLightAnimationData getAnimationData() {
        return this.data;
    }

    public void init() {
        this.timeAddBtn = GuiUtils.createButton(52, 4, 16, 16, GuiUtils.text("+"), (p) -> {
            this.data.addDurationSeconds(1);
            this.timeInput.setValue(Integer.toString((int)this.data.getDurationSeconds()));
        });

        this.timeRemoveBtn = GuiUtils.createButton(4, 4, 16, 16, GuiUtils.text("-"), (p) -> {
            this.data.subDurationSeconds(1);
            this.timeInput.setValue(Integer.toString((int)this.data.getDurationSeconds()));
        });

        this.timeInput = GuiUtils.createEditBox(21, 5, 30, 14, this.parent.getFont(), Integer.toString((int)this.data.getDurationSeconds()), true,
        (text) -> {
            try {
                this.data.setDurationSeconds((double)Integer.parseInt(text));
            } catch (NumberFormatException e) {}
        }, null);
        this.timeInput.setMaxLength(3);
        this.timeInput.setFilter(input -> {
                if (input.isEmpty())
                    return true;

                try {
                    int i = Integer.parseInt(input);
                    input = Integer.toString(Mth.clamp(i, 0, TrafficLightAnimationData.MAX_SECONDS));
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        );

        this.idInput = GuiUtils.createEditBox(77, 5, 30, 14, this.parent.getFont(), Integer.toString(this.data.getPhaseId()), true,
        (text) -> {
            try {
                this.data.setPhaseId(Integer.parseInt(text));
            } catch (NumberFormatException e) {}
        }, null);
        this.idInput.setMaxLength(3);
        this.idInput.setEditable(idEditable);
        this.idInput.setFilter(input -> {
                if (input.isEmpty())
                    return true;

                try {
                    Integer.parseInt(input);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        );

        this.modeButton = GuiUtils.createCycleButton(ModMain.MOD_ID, TrafficLightMode.class, MODE_BTN_X_OFFSET, MODE_BTN_Y_OFFSET, MODE_BTN_W, MODE_BTN_H, textMode, this.data.getMode(), 
        (btn, value) -> {
            this.data.setMode(value);
        });

        this.renderableWidgets.clear();

        this.renderableWidgets.add(new WidgetData(timeAddBtn));
        this.renderableWidgets.add(new WidgetData(timeRemoveBtn));
        this.renderableWidgets.add(new WidgetData(timeInput));
        this.renderableWidgets.add(new WidgetData(idInput));
        this.renderableWidgets.add(new WidgetData(modeButton));

        for (WidgetData widget : renderableWidgets) {
            this.parent.addWidget(widget.getWidget());
        }
    }

    public void render(int yOffset, PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        this.y = yOffset;

        int y = selected ? 2 * height : 0;
        GuiUtils.blit(WIDGETS, stack, this.x, this.y, 0, y, width, height);

        for (WidgetData widget : renderableWidgets) {
            widget.renderWithOffset(stack, mouseX, mouseY, partialTicks, this.x, this.y);
        }        
    }
}
