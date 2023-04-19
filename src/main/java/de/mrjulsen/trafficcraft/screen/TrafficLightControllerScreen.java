package de.mrjulsen.trafficcraft.screen;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.TrafficLightControlType;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightControllerPacket;
import de.mrjulsen.trafficcraft.screen.widgets.IListEntryData;
import de.mrjulsen.trafficcraft.screen.widgets.ParentableScreen;
import de.mrjulsen.trafficcraft.screen.widgets.data.TrafficLightAnimationData;
import de.mrjulsen.trafficcraft.screen.widgets.data.TrafficLightSchedule;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficLightControllerScreen extends ParentableScreen
{
    public static final Component title = new TextComponent("trafficlightsettings");
    
    private int guiTop = 50;

    private static final int HEIGHT = 150;


    private BlockPos blockPos;
    private Level level;
    
    // Settings
    private boolean status;
    private TrafficLightSchedule schedule;

    // Controls    
    protected CycleButton<TrafficLightControlType> controlTypeButton;

    protected CycleButton<Boolean> statusButton;
    protected Button editScheduleButton;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.trafficlightcontroller.title");
    private TranslatableComponent textStatus = new TranslatableComponent("gui.trafficcraft.trafficlightcontroller.status");
    private TranslatableComponent textEditSchedule = new TranslatableComponent("gui.trafficcraft.trafficlightcontroller.edit_schedule");

    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    public TrafficLightControllerScreen(BlockPos pos, Level level)
    {
        super(title);
        this.level = level;
        this.blockPos = pos;

        if (this.level.getBlockEntity(blockPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
            this.schedule = blockEntity.getFirstOrMainSchedule();
            this.status = blockEntity.isRunning();
        }

    }

    public TrafficLightControllerBlockEntity getBlockEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightControllerBlockEntity ? (TrafficLightControllerBlockEntity)be : null;
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

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 100, 97, 20, btnDoneTxt, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 3, guiTop + 100, 97, 20, btnCancelTxt, (p) -> {
            this.onClose();
        }));

        this.editScheduleButton = new Button(this.width / 2 - 100, guiTop + 30, 200, 20, textEditSchedule, (p) -> {
            this.minecraft.setScreen(new TrafficLightScheduleScreen(this, blockPos, level, true));
        });
        this.addRenderableWidget(editScheduleButton); 


        this.statusButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.status)
        .create(this.width / 2 - 100, guiTop + 55, 200, 20, textStatus, (pCycleButton, pValue) -> {
            this.status = pValue;
        }));
    }

    private void onDone() {
        NetworkManager.MOD_CHANNEL.sendToServer(new TrafficLightControllerPacket(
            blockPos,
            status
        ));

        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {        
        renderBackground(stack, 0);
        
        drawCenteredString(stack, this.font, textTitle, this.width / 2, guiTop, 16777215);
        
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
    public <T extends Entry<T>> void buildList(Consumer<T> modListViewConsumer, Function<IListEntryData, T> newEntry) {
        for (int i = 0; i < 50; i++) {
            modListViewConsumer.accept(newEntry.apply(new TrafficLightAnimationData()));
        }
    }

}
