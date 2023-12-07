package de.mrjulsen.trafficcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightDirection;
import de.mrjulsen.trafficcraft.block.data.TrafficLightMode;
import de.mrjulsen.trafficcraft.block.data.TrafficLightVariant;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.TrafficLightPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficLightConfigScreen extends CommonScreen
{
    public static final Component title = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.title");
    
    private int guiTop = 50;
    private static final int LINES = 5;

    private static final int SPACING_X = 3;
    private static final int SPACING_Y = 25;

    private static final int HEIGHT = (int)((LINES + 2.5) * SPACING_Y);


    private BlockPos blockPos;
    private Level level;
    private TrafficLightControlType controlType;
    private TrafficLightMode mode;
    private TrafficLightVariant variant;
    private TrafficLightDirection direction;
    private boolean status;
    
    private boolean isLinked;
    private Location linkLocation;

    // Controls    
    protected ResizableCycleButton<TrafficLightControlType> controlTypeButton;

    protected EditBox idInput;
    protected ResizableCycleButton<TrafficLightMode> modeButton;
    protected ResizableCycleButton<TrafficLightVariant> variantButton;
    protected ResizableCycleButton<TrafficLightDirection> directionButton;
    protected Button editScheduleButton;
    protected ResizableCycleButton<Boolean> statusButton;

    private TranslatableComponent textEditSchedule = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.edit_schedule");
    private TranslatableComponent textId = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.id");
    private TranslatableComponent textMode = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.mode");
    private TranslatableComponent textVariant = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.variant");
    private TranslatableComponent textDirection = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.direction");
    private TranslatableComponent textControlType = GuiUtils.translate("gui.trafficcraft.trafficlightsettings.controltype");
    private TranslatableComponent textStatus = GuiUtils.translate("gui.trafficcraft.trafficlightcontroller.status");

    public TrafficLightConfigScreen(BlockPos pos, Level level) {
        super(title);
        this.level = level;
        this.blockPos = pos;

        BlockState state = this.level.getBlockState(blockPos);
        mode = state.getValue(TrafficLightBlock.MODE);
        variant = state.getValue(TrafficLightBlock.VARIANT);
        direction = state.getValue(TrafficLightBlock.DIRECTION);
        
        readBlockEntity();
    }


    public TrafficLightBlockEntity getBlockEntity() {
        BlockEntity be = level.getBlockEntity(blockPos);
        return be instanceof TrafficLightBlockEntity ? (TrafficLightBlockEntity)be : null;
    }

    private void updateControlType(TrafficLightControlType newControlType) {
        this.controlType = newControlType;
        this.updatePage();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.controlType == TrafficLightControlType.REMOTE) {
            this.idInput.tick();
        }        
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void readBlockEntity() {
        if (this.level.getBlockEntity(blockPos) instanceof TrafficLightBlockEntity blockEntity) {
            this.controlType = blockEntity.getControlType();
            this.status = blockEntity.isRunning();
            //TODO: change
            //this.isLinked = blockEntity.isValidLinked();
            //this.linkLocation = blockEntity.getLinkLocation();
        }
    }

    @Override
    public void init() {
        super.init();
        
        guiTop = this.height / 2 - HEIGHT / 2;


        /* Default page */

        addButton(this.width / 2 - 100, guiTop + (int)(SPACING_Y * 6.5f), 100 - SPACING_X, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addButton(this.width / 2 + SPACING_X, guiTop + (int)(SPACING_Y * 6.5f), 100 - SPACING_X, 20, CommonComponents.GUI_CANCEL, (p) -> {
            this.onClose();
        }, null);        
        
        this.variantButton = addCycleButton(ModMain.MOD_ID, TrafficLightVariant.class, this.width / 2 - 150, guiTop + SPACING_Y * 1, 300, 20, textVariant, variant,
        (btn, value) -> {
            this.variant = value;

            this.directionButton.active = value != TrafficLightVariant.PEDESTRIAN;
            if (value == TrafficLightVariant.PEDESTRIAN) {
                this.direction = TrafficLightDirection.NORMAL;
                this.directionButton.setValue(TrafficLightDirection.NORMAL);
            }
        }, null);

        this.directionButton = addCycleButton(ModMain.MOD_ID, TrafficLightDirection.class, this.width / 2 - 150, guiTop + SPACING_Y * 2, 300, 20, textDirection, direction,
        (btn, value) -> {
            this.direction = value;
        }, null);
        this.directionButton.active = variant != TrafficLightVariant.PEDESTRIAN;

        this.controlTypeButton = addCycleButton(ModMain.MOD_ID, TrafficLightControlType.class, this.width / 2 - 150, guiTop + SPACING_Y * 3, 300, 20, textControlType, controlType,
        (btn, value) -> {
            this.updateControlType(value);
        }, Tooltip.of(GuiUtils.getEnumTooltipData(ModMain.MOD_ID, this, TrafficLightControlType.class, width / 4)));


        /* STATIC PAGE */
        this.modeButton = addCycleButton(ModMain.MOD_ID, TrafficLightMode.class, this.width / 2 - 150, guiTop + SPACING_Y * 4, 300, 20, textMode, mode,
        (btn, value) -> {
            this.mode = value;
        }, null);

        /* REMOTE PAGE */
        this.idInput = addEditBox(this.width / 2 + SPACING_X, guiTop + SPACING_Y * 4, 60, 16, Integer.toString(this.getBlockEntity().getPhaseId()), true,
        (text) -> { }, NO_EDIT_BOX_FOCUS_CHANGE_ACTION, null);
        this.idInput.setMaxLength(5);
        this.idInput.setFilter(input -> {
            if (input.isEmpty()) {
                return true;
            }

            try {
                Integer.parseInt(input);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }); 

        /* OWN SCHEDULE PAGE */
        this.editScheduleButton = addButton(this.width / 2 - 100, guiTop + SPACING_Y * 4, 200, 20, textEditSchedule, (p) -> {
            this.minecraft.setScreen(new TrafficLightScheduleScreen(this, blockPos, level, false));
        }, null); 

        this.statusButton = addOnOffButton(this.width / 2 - 100, guiTop + SPACING_Y * 5, 200, 20, textStatus, status,
        (btn, value) -> {
            this.status = value;
        }, null);
        
        this.updatePage();
    }

    @Override
    protected void onDone() {
        NetworkManager.MOD_CHANNEL.sendToServer(new TrafficLightPacket(
            blockPos,
            Integer.parseInt(idInput.getValue()),
            mode.getIndex(),
            variant.getIndex(),
            direction.getIndex(),
            controlType.getIndex(),
            this.status
        ));
        this.onClose();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(stack, 0);
        
        /* DEFAULT PAGE */
        drawCenteredString(stack, this.font, getTitle(), this.width / 2, guiTop, 0xFFFFFF);        
        
        switch (this.controlType) {
            case REMOTE:
                drawString(stack, this.font, textId, this.width / 2 - this.font.width(textId) - SPACING_X, guiTop + 25 * 4 + 8 - this.font.lineHeight / 2, 16777215);
                
                if (isLinked) {
                    drawCenteredString(stack, this.font, GuiUtils.translate("gui.trafficcraft.trafficlightsettings.linked", this.linkLocation.x, this.linkLocation.y, this.linkLocation.z, this.linkLocation.dimension), this.width / 2, guiTop + SPACING_Y * 5, 0x55FF55);
                } else {
                    drawCenteredString(stack, this.font, GuiUtils.translate("gui.trafficcraft.trafficlightsettings.not_linked"), this.width / 2, guiTop + SPACING_Y * 5, 0xFF5555);
                }

                this.idInput.render(stack, mouseX, mouseY, partialTicks);
                break;
            default:
                break;
        }
        
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    private void updatePage() {
        this.modeButton.visible = false;

        this.idInput.setVisible(false);

        this.editScheduleButton.visible = false;
        this.statusButton.visible = false;

        switch (this.controlType) {
            case STATIC:
                this.modeButton.visible = true;
                break;
            case OWN_SCHEDULE:  
                this.editScheduleButton.visible = true;
                this.statusButton.visible = true;
                break;
            case REMOTE:
                this.idInput.setVisible(true);
                break;
            default:
                break;
        }
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

}
