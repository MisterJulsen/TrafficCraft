package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.widgets.TrafficLightScheduleEntry;
import de.mrjulsen.trafficcraft.client.widgets.data.WidgetData;
import de.mrjulsen.trafficcraft.data.TrafficLightAnimationData;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficLightScheduleScreen extends CommonScreen {
    public static final Component title = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.title");
    
    private int guiTop;    
    private int guiLeft;
    private static final int WIDTH = 237;
    private static final int HEIGHT = 211;

    private static final int MAX_ENTRIES_ON_PAGE = 5;
    private int scroll = 0;
    private int maxScroll = 0;
    private float currentScroll = 0.0f;
    private boolean isScrolling = false;

    private final boolean isEditable;
    private final boolean fromController;


    private final BlockPos blockPos;
    private final Level level;
    private final Screen lastScreen;

    // Settings
    private TrafficLightSchedule schedule;

    // Controls    
    protected ResizableCycleButton<TrafficLightTrigger> triggerBtn;
    protected ResizableCycleButton<Boolean> loopBtn;
    protected List<TrafficLightScheduleEntry> animationEntries = new ArrayList<TrafficLightScheduleEntry>();
    private TrafficLightScheduleEntry selectedEntry;
    private Button removeBtn;
    private Button moveUpBtn;
    private Button moveDownBtn;

    private final Component textTrigger = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.trigger");
    private final Component textLoop = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.loop");    
    private final Component columnTime = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.column_time");
    private final Component columnId = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.column_id");
    private final Component columnMode = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.column_mode");
    private final Component tooltipColTime = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.col_time");
    private final Component tooltipColId = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.col_id");
    private final Component tooltipColMode = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.col_mode");
    private final Component tooltipTrigger = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.trigger");
    private final Component tooltipLoop = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.loop");
    private final Component tooltipAdd = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.entry_add");
    private final Component tooltipRemove = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.entry_remove");
    private final Component tooltipMoveUp = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.move_up");
    private final Component tooltipMoveDown = GuiUtils.translate("gui.trafficcraft.trafficlightschedule.tooltip.move_down");

    private static final ResourceLocation GUI = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_schedule.png");
    private static final ResourceLocation WIDGETS = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_schedule_widgets.png");

    public TrafficLightScheduleScreen(Screen lastScreen, BlockPos pos, Level level, boolean isEditable) {
        super(title);
        this.lastScreen = lastScreen;
        this.level = level;
        this.isEditable = isEditable;
        this.blockPos = pos;
        this.fromController = isController();

        if (fromController) {            
            this.schedule = this.getBlockControllerEntity().getFirstOrMainSchedule();
        } else {
            this.schedule = this.getBlockEntity().getSchedule();
        }
    }

    private boolean isController() {
        return level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity;
    }

    public TrafficLightControllerBlockEntity getBlockControllerEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightControllerBlockEntity ? (TrafficLightControllerBlockEntity)be : null;
    }

    public TrafficLightBlockEntity getBlockEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightBlockEntity ? (TrafficLightBlockEntity)be : null;
    }

    public Font getFont() {
        return this.font;
    }

    public void updateScroll() {
        this.maxScroll = Math.max(this.animationEntries.size() - MAX_ENTRIES_ON_PAGE, 0);
    }

    private void setSelectedEntry(TrafficLightScheduleEntry entry) {        
        this.selectedEntry = entry;
        this.removeBtn.active = entry != null;
        this.moveUpBtn.active = entry != null;
        this.moveDownBtn.active = entry != null;
    }

    public void reinitEntries(boolean removeWidgets) {
        if (removeWidgets) {
            for (TrafficLightScheduleEntry entry : this.animationEntries) {
                for (WidgetData data : entry.renderableWidgets) {
                    this.removeWidget(data.getWidget());
                }
                entry.renderableWidgets.clear();
            }
        }
        this.animationEntries.clear();

        for (TrafficLightAnimationData data : this.schedule.getEntries()) {            
            TrafficLightScheduleEntry entry = new TrafficLightScheduleEntry(this, guiLeft + 9, guiTop, 205, 24, this.isEditable, data);
            this.animationEntries.add(entry);
            entry.init();
        }
        this.setSelectedEntry(null);
        this.updateScroll();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        
        guiTop = this.height / 2 - HEIGHT / 2;
        guiLeft = this.width / 2 - WIDTH / 2;

        animationEntries.clear();

        addButton(guiLeft + WIDTH - 98, guiTop + 183, 90, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        /* EDIT BUTTONS */
        addButton(guiLeft + 8, guiTop + 183, 20, 20, GuiUtils.text("+"), (p) -> {
            TrafficLightAnimationData data = new TrafficLightAnimationData();
            this.schedule.getEntries().add(data);
            this.reinitEntries(true);
        }, Tooltip.of(tooltipAdd));

        this.removeBtn = addButton(guiLeft + 31, guiTop + 183, 20, 20, GuiUtils.text("-"), (p) -> {
            if (this.selectedEntry != null) {
                this.schedule.getEntries().remove(selectedEntry.getAnimationData());
                this.reinitEntries(true);
            }
        }, Tooltip.of(tooltipRemove));
        this.removeBtn.active = false;

        this.moveUpBtn = addButton(guiLeft + 58, guiTop + 183, 20, 20, GuiUtils.text("↑"), (p) -> {
            if (this.selectedEntry != null) {
                int i = this.moveUp(this.schedule.getEntries().indexOf(selectedEntry.getAnimationData()));
                if (i >= 0) {
                    this.reinitEntries(true);
                    onEntrySelect(animationEntries.get(i), true);
                }
            }
        }, Tooltip.of(tooltipMoveUp));
        this.moveUpBtn.active = false;

        this.moveDownBtn = addButton(guiLeft + 81, guiTop + 183, 20, 20, GuiUtils.text("↓"), (p) -> {
            if (this.selectedEntry != null) {
                int i = this.moveDown(this.schedule.getEntries().indexOf(selectedEntry.getAnimationData()));
                if (i >= 0) {
                    this.reinitEntries(true);
                    onEntrySelect(animationEntries.get(i), true);
                }
            }
        }, Tooltip.of(tooltipMoveDown));
        this.moveDownBtn.active = false;

        this.triggerBtn = addCycleButton(ModMain.MOD_ID, TrafficLightTrigger.class, guiLeft + 8, guiTop + 20, 109, 16, textTrigger, this.schedule.getTrigger(),
        (btn, value) -> {
            this.schedule.setTrigger(value);
        }, Tooltip.of(GuiUtils.getEnumTooltipData(ModMain.MOD_ID, this, TrafficLightTrigger.class, width / 4)).withMaxWidth(width / 4));

        this.loopBtn = addOnOffButton(guiLeft + 8 + 113, guiTop + 20, 109, 16, textLoop, this.schedule.isLoop(),
        (btn, value) -> {
            this.schedule.setLoop(value);
        }, Tooltip.of(tooltipLoop).withMaxWidth(width / 4));

        this.reinitEntries(false);
        this.updateScroll();

    }

    public void addWidget(AbstractWidget w) {
        super.addWidget(w);
    }

    @Override
    protected void onDone() {  
        List<TrafficLightSchedule> schedules = new ArrayList<>();
        schedules.add(schedule);
        NetworkManager.getInstance().send(new TrafficLightSchedulePacket(
            blockPos,
            schedules
        ), null);
        this.onClose();
    }

    private int moveUp(int currentIndex) {
        if (currentIndex >= this.schedule.getEntries().size() || currentIndex <= 0)
            return -1;

        TrafficLightAnimationData data = this.schedule.getEntries().remove(currentIndex);
        this.schedule.getEntries().add(currentIndex - 1, data);
        return currentIndex - 1;
    }

    private int moveDown(int currentIndex) {
        if (currentIndex >= this.schedule.getEntries().size() - 1 || currentIndex < 0)
            return -1;

        TrafficLightAnimationData data = this.schedule.getEntries().remove(currentIndex);
        this.schedule.getEntries().add(currentIndex + 1, data);
        return currentIndex + 1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {        
        if(button == 0 && scrollbarClamp(mouseX, mouseY) && maxScroll > 0)
            isScrolling = true;

        boolean b = super.mouseClicked(mouseX, mouseY, button);

        if(mouseX > guiLeft + 9 && mouseX < guiLeft + 9 + 205 && mouseY > guiTop + 57 && mouseY < guiTop + 177)
        {
            if(button == 0)
            {
                int choice = (int)(mouseY - (guiTop + 57)) / 24;
                if (choice < this.animationEntries.size()) {                    
                    this.onEntrySelect(this.animationEntries.get(choice + scroll), b ? true : !this.animationEntries.get(choice + scroll).selected);
                }
            }
        }

        return b;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {        
        renderBackground(stack, 0);
        
        /* BASE MENU */
        GuiUtils.blit(GUI, stack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);
        if (maxScroll > 0) {
            GuiUtils.blit(GUI, stack, guiLeft + WIDTH - 21, (int)(guiTop + 41 + 121 * this.currentScroll), 0, 211, 12, 15);
        } else {
            GuiUtils.blit(GUI, stack, guiLeft + WIDTH - 21, guiTop + 41, 12, 211, 12, 15);
        }
        
        GuiUtils.blit(WIDGETS, stack, guiLeft + 9, guiTop + 41, 0, 72, 205, 16);
        

        String title = getTitle().getString();
        String time = columnTime.getString();
        String id = columnId.getString();
        String mode = columnMode.getString();
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);
        this.font.draw(stack, time, guiLeft + 9 + 72 / 2 - font.width(time) / 2, guiTop + 45, 4210752);
        this.font.draw(stack, id, guiLeft + 9 + 72 + 40 / 2 - font.width(id) / 2, guiTop + 45, 4210752);
        this.font.draw(stack, mode, guiLeft + 9 + 72 + 40 + 93 / 2 - font.width(mode) / 2, guiTop + 45, 4210752);

        for (int i = 0; i < MAX_ENTRIES_ON_PAGE; i++) {
            if (i + scroll >= this.animationEntries.size())
                continue;

            TrafficLightScheduleEntry entry = this.animationEntries.get(i + scroll);
            int posY = guiTop + 57 + i * entry.height;
            entry.render(posY, stack, mouseX, mouseY, partialTicks);
        }


        if (mouseX > guiLeft + 9 && mouseX < guiLeft + 9 + 72 && mouseY > guiTop + 41 && mouseY < guiTop + 57) {
            renderTooltip(stack, this.font.split(tooltipColTime, width / 2), mouseX, mouseY);
        } else if (mouseX > guiLeft + 9 + 72 && mouseX < guiLeft + 9 + 72 + 40 && mouseY > guiTop + 41 && mouseY < guiTop + 57) {
            renderTooltip(stack, this.font.split(tooltipColId, width / 2), mouseX, mouseY);
        } else if (mouseX > guiLeft + 9 + 72 + 40 && mouseX < guiLeft + 9 + 72 + 40 + 93 && mouseY > guiTop + 41 && mouseY < guiTop + 57) {
            renderTooltip(stack, this.font.split(tooltipColMode, width / 2), mouseX, mouseY);
        }
       
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

    
    
    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        if (maxScroll > 0) {
            scroll -= p_mouseScrolled_5_;
            if(scroll < 0)
                scroll = 0;
            else if(scroll > maxScroll)
                scroll = maxScroll;

            int i = maxScroll;
            this.currentScroll = (float)((double)this.currentScroll - p_mouseScrolled_5_ / (double)i);
            this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_)
    {
        if(this.isScrolling)
        {
            int i = this.guiTop + 41;
            int j = i + 136;
            this.currentScroll = ((float)p_mouseDragged_3_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
            scroll = (int)((currentScroll + 0.01) * maxScroll);
            if(scroll < 0)
                scroll = 0;
            return true;
        }
        else
        {
            return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
    {
        if(p_mouseReleased_5_ == 0)
        {
            this.isScrolling = false;
        }

        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected boolean scrollbarClamp(double mouseX, double mouseY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        int k = i + 216;
        int l = j + 41;
        int i1 = k + 13;
        int j1 = l + 136;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)i1 && mouseY < (double)j1;
    }

    public void onEntrySelect(TrafficLightScheduleEntry entry, boolean select) {
        for (TrafficLightScheduleEntry e : animationEntries) {
            e.selected = false;
        }
        entry.selected = select;
        this.setSelectedEntry(select ? entry : null);
    }

    @Override
    public void onClose() {
        if (lastScreen != null) {            
            this.minecraft.setScreen(this.lastScreen);
        } else {
            super.onClose();
        }
    }
}
