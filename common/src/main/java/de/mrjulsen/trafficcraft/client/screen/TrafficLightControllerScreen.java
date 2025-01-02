package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.data.Clipboard;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.ModGuiUtils;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficLightControllerScreen extends DLScreen {
    public static final Component title = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.title");

    private static final int GUI_WIDTH = 240;
    
    private int guiTop = 50;

    private static final int HEIGHT = 150;


    private BlockPos blockPos;
    private Level level;
    
    // Settings
    private boolean status;
    private DLIconButton pasteButton;

    // Controls
    protected DLCycleButton<Boolean> statusButton;
    protected DLButton editScheduleButton;

    private Component textStatus = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.status");
    private Component textEditSchedule = TextUtils.translate("gui.trafficcraft.trafficlightcontroller.edit_schedule");

    public TrafficLightControllerScreen(BlockPos pos, Level level) {
        super(title);
        this.level = level;
        this.blockPos = pos;

        if (this.level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
            this.status = blockEntity.isRunning();
        }
    }

    public TrafficLightControllerBlockEntity getBlockEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightControllerBlockEntity ? (TrafficLightControllerBlockEntity)be : null;
    }

    @Override
    public void init() {
        super.init();        
        guiTop = this.height / 2 - HEIGHT / 2;
        

        addButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 100, GUI_WIDTH / 2 - 3, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + 3, guiTop + 100, GUI_WIDTH / 2 - 2, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        this.editScheduleButton = addButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 30, GUI_WIDTH - 2 * (DLIconButton.DEFAULT_BUTTON_WIDTH + 2), 20, textEditSchedule, (p) -> {
            this.minecraft.setScreen(new TrafficLightScheduleEditor(this, level, blockPos));
        }, null);

        this.statusButton = addOnOffButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 55, GUI_WIDTH, 20, textStatus, status,
        (btn, value) -> {
            this.status = value;
        }, null);

        // copy
        DLIconButton copyBtn = addRenderableWidget(ModGuiUtils.createCopyButton(
            this.width / 2 + GUI_WIDTH / 2 - 2 * (DLIconButton.DEFAULT_BUTTON_WIDTH + 2),
            guiTop + 30,
            DLIconButton.DEFAULT_BUTTON_WIDTH + 2,
            DLIconButton.DEFAULT_BUTTON_HEIGHT + 2,
            null,
            AreaStyle.NATIVE,
            (btn) -> {
                if (level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                    Clipboard.put(TrafficLightSchedule.class, blockEntity.getFirstOrMainSchedule());
                }
            })
        );
        addTooltip(DLTooltip.of(Constants.textCopy).assignedTo(copyBtn));
        // paste
        pasteButton = addRenderableWidget(ModGuiUtils.createPasteButton(
            this.width / 2 + GUI_WIDTH / 2 - (DLIconButton.DEFAULT_BUTTON_WIDTH + 2),
            guiTop + 30,
            DLIconButton.DEFAULT_BUTTON_WIDTH + 2,
            DLIconButton.DEFAULT_BUTTON_HEIGHT + 2,
            null,
            AreaStyle.NATIVE,
            (btn) ->  {
                Optional<TrafficLightSchedule> schedule = Clipboard.get(TrafficLightSchedule.class);
                if (schedule.isPresent()) {
                    TrafficCraft.net().sendToServer(new TrafficLightSchedulePacket(
                        blockPos,
                        List.of(schedule.get())
                    ));
                }
            })
        );
        pasteButton.set_active(false);
        addTooltip(DLTooltip.of(Constants.textPaste).assignedTo(pasteButton));
    }

    @Override
    protected void onDone() {
        TrafficCraft.net().sendToServer(new TrafficLightControllerPacket(
            blockPos,
            status
        ));

        this.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        pasteButton.set_active(Clipboard.contains(TrafficLightSchedule.class));
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        GuiUtils.drawString(graphics, font, this.width / 2, guiTop, title, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.CENTER, false);
    }
}
