package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Clipboard;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.ModGuiUtils;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficLightControllerScreen extends CommonScreen {
    public static final Component title = Utils.translate("gui.trafficcraft.trafficlightcontroller.title");

    private static final int GUI_WIDTH = 240;
    
    private int guiTop = 50;

    private static final int HEIGHT = 150;


    private BlockPos blockPos;
    private Level level;
    
    // Settings
    private boolean status;
    private IconButton pasteButton;

    // Controls
    protected ResizableCycleButton<Boolean> statusButton;
    protected Button editScheduleButton;

    private Component textStatus = Utils.translate("gui.trafficcraft.trafficlightcontroller.status");
    private Component textEditSchedule = Utils.translate("gui.trafficcraft.trafficlightcontroller.edit_schedule");

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
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        
        guiTop = this.height / 2 - HEIGHT / 2;


        /* Default page */

        addButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 100, GUI_WIDTH / 2 - 3, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + 3, guiTop + 100, GUI_WIDTH / 2 - 2, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        this.editScheduleButton = addButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 30, GUI_WIDTH - 2 * (IconButton.DEFAULT_BUTTON_WIDTH + 2), 20, textEditSchedule, (p) -> {
            this.minecraft.setScreen(new TrafficLightScheduleEditor(this, level, blockPos));
        }, null);

        this.statusButton = addOnOffButton(this.width / 2 - GUI_WIDTH / 2, guiTop + 55, GUI_WIDTH, 20, textStatus, status,
        (btn, value) -> {
            this.status = value;
        }, null);

        // copy
        IconButton copyBtn = addRenderableWidget(ModGuiUtils.createCopyButton(
            this.width / 2 + GUI_WIDTH / 2 - 2 * (IconButton.DEFAULT_BUTTON_WIDTH + 2),
            guiTop + 30,
            IconButton.DEFAULT_BUTTON_WIDTH + 2,
            IconButton.DEFAULT_BUTTON_HEIGHT + 2,
            null,
            AreaStyle.NATIVE,
            (btn) -> {
                if (level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
                    Clipboard.put(TrafficLightSchedule.class, blockEntity.getFirstOrMainSchedule());
                }
            })
        );
        addTooltip(Tooltip.of(Constants.textCopy).assignedTo(copyBtn));
        // paste
        pasteButton = addRenderableWidget(ModGuiUtils.createPasteButton(
            this.width / 2 + GUI_WIDTH / 2 - (IconButton.DEFAULT_BUTTON_WIDTH + 2),
            guiTop + 30,
            IconButton.DEFAULT_BUTTON_WIDTH + 2,
            IconButton.DEFAULT_BUTTON_HEIGHT + 2,
            null,
            AreaStyle.NATIVE,
            (btn) ->  {
                Optional<TrafficLightSchedule> schedule = Clipboard.get(TrafficLightSchedule.class);
                if (schedule.isPresent()) {
                    NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new TrafficLightSchedulePacket(
                        blockPos,
                        List.of(schedule.get())
                    ));
                }
            })
        );
        pasteButton.active = false;
        addTooltip(Tooltip.of(Constants.textPaste).assignedTo(pasteButton));
    }

    @Override
    protected void onDone() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new TrafficLightControllerPacket(
            blockPos,
            status
        ));

        this.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        pasteButton.active = Clipboard.contains(TrafficLightSchedule.class);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(stack);
        
        drawCenteredString(stack, this.font, getTitle(), this.width / 2, guiTop, 0xFFFFFFFF);
        
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
       if ((this.shouldCloseOnEsc() && pKeyCode == InputConstants.KEY_ESCAPE) || (!(getFocused() instanceof EditBox) && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode)))) {
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

}
