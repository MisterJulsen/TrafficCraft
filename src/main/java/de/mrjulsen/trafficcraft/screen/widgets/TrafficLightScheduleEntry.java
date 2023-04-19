package de.mrjulsen.trafficcraft.screen.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.properties.TrafficLightMode;
import de.mrjulsen.trafficcraft.screen.TrafficLightScheduleScreen;
import de.mrjulsen.trafficcraft.screen.widgets.data.TrafficLightAnimationData;
import de.mrjulsen.trafficcraft.screen.widgets.data.WidgetData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
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
    protected ResizableButton timeAddBtn;
    protected ResizableButton timeRemoveBtn;
    protected ResizableCycleButton<TrafficLightMode> modeButton;

    private boolean idEditable;

    public int x, y, width, height;

    public boolean selected;

    public List<WidgetData> renderableWidgets = new ArrayList<WidgetData>();

    private TranslatableComponent textMode = new TranslatableComponent("gui.trafficcraft.trafficlightsettings.mode");

    private TranslatableComponent tooltipAdd = new TranslatableComponent("gui.trafficcraft.trafficlightschedule.tooltip.time_add");
    private TranslatableComponent tooltipRemove = new TranslatableComponent("gui.trafficcraft.trafficlightschedule.tooltip.time_remove");
    
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
        this.timeAddBtn = new ResizableButton(52, 4, 16, 16, new TextComponent("+"), (p) -> {
            this.data.addDurationSeconds(1);
            this.timeInput.setValue(Integer.toString((int)this.data.getDurationSeconds()));
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            this.parent.renderTooltip(pPoseStack, tooltipAdd, pMouseX, pMouseY);
        });

        this.timeRemoveBtn = new ResizableButton(4, 4, 16, 16, new TextComponent("-"), (p) -> {
            this.data.subDurationSeconds(1);
            this.timeInput.setValue(Integer.toString((int)this.data.getDurationSeconds()));
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            this.parent.renderTooltip(pPoseStack, tooltipRemove, pMouseX, pMouseY);
        });

        this.timeInput = new EditBox(this.parent.getFont(), 21, 5, 30, 14, new TranslatableComponent("gui.trafficcraft.trafficlightsettings.duration"));
        this.timeInput.setMaxLength(3);
        this.timeInput.setValue(Integer.toString((int)this.data.getDurationSeconds()));
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
        this.timeInput.setResponder(pResponse -> {
            try {
                this.data.setDurationSeconds((double)Integer.parseInt(pResponse));
            } catch (NumberFormatException e) {}
        });

        this.idInput = new EditBox(this.parent.getFont(), 77, 5, 30, 14, new TranslatableComponent("gui.trafficcraft.trafficlightsettings.id"));
        this.idInput.setMaxLength(3);
        this.idInput.setValue(Integer.toString(this.data.getPhaseId()));
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
        this.idInput.setResponder(pResponse -> {
            try {
                this.data.setPhaseId(Integer.parseInt(pResponse));
            } catch (NumberFormatException e) {}
        });

        this.modeButton = ResizableCycleButton.<TrafficLightMode>builder((p) -> {            
            return new TranslatableComponent(p.getTranslationKey());
        })
            .withValues(TrafficLightMode.values()).displayOnlyValue().withInitialValue(this.data.getMode())            
            .create(MODE_BTN_X_OFFSET, MODE_BTN_Y_OFFSET, MODE_BTN_W, MODE_BTN_H, textMode, (pCycleButton, pValue) -> {
                this.data.setMode(pValue);    
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

    public void render(int yOffset, PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {        
        this.y = yOffset;

        RenderSystem.setShaderTexture(0, WIDGETS);
        int y = selected ? 2 * height : 0;
        this.parent.blit(stack, this.x, this.y, 0, y, width, height);

        for (WidgetData widget : renderableWidgets) {
            widget.renderWithOffset(stack, mouseX, mouseY, partialTicks, this.x, this.y);
        }        
    }
}
