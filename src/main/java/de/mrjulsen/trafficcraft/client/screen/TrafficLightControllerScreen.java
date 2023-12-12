package de.mrjulsen.trafficcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.network.NewNetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightControllerPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficLightControllerScreen extends CommonScreen {
    public static final Component title = GuiUtils.translate("gui.trafficcraft.trafficlightcontroller.title");
    
    private int guiTop = 50;

    private static final int HEIGHT = 150;


    private BlockPos blockPos;
    private Level level;
    
    // Settings
    private boolean status;

    // Controls
    protected ResizableCycleButton<Boolean> statusButton;
    protected Button editScheduleButton;

    private TranslatableComponent textStatus = GuiUtils.translate("gui.trafficcraft.trafficlightcontroller.status");
    private TranslatableComponent textEditSchedule = GuiUtils.translate("gui.trafficcraft.trafficlightcontroller.edit_schedule");

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

        addButton(this.width / 2 - 100, guiTop + 100, 97, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + 3, guiTop + 100, 97, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);

        this.editScheduleButton = addButton(this.width / 2 - 100, guiTop + 30, 200, 20, textEditSchedule, (p) -> {
            this.minecraft.setScreen(new TrafficLightScheduleScreen(this, blockPos, level, true));
        }, null);

        this.statusButton = addOnOffButton(this.width / 2 - 100, guiTop + 55, 200, 20, textStatus, status,
        (btn, value) -> {
            this.status = value;
        }, null);
    }

    @Override
    protected void onDone() {
        NewNetworkManager.getInstance().send(new TrafficLightControllerPacket(
            blockPos,
            status
        ), null);

        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(stack, 0);
        
        drawCenteredString(stack, this.font, getTitle(), this.width / 2, guiTop, 16777215);
        
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            this.onClose();
            return true;
        }
        else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

}
