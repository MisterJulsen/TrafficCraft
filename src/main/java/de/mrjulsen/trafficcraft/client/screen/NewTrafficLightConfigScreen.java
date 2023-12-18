package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.DragonLibConstants;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.gui.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.mcdragonlib.client.gui.Tooltip;
import de.mrjulsen.mcdragonlib.client.gui.WidgetsCollection;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.gui.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.Alignment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.AbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager.TrafficLightTextureKey;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightPacket;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.EmptyModelData;

public class NewTrafficLightConfigScreen extends CommonScreen {

    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(ModMain.MOD_ID, "textures/gui/window_arrow.png");
    private static final int WINDOW_WIDTH = 241;
    private static final int WINDOW_HEIGHT = 230;
    private static final int WINDOW_PADDING_LEFT = 69;
    private static final int INNER_PADDING = 7;
    private static final int INNER_TOP_PADDING = 20;
    private static final int TRAFFIC_LIGHT_LIGHT_SIZE = 24;
    private static final int GLOBAL_SETTINGS_INDEX = -1;
    private static final float SMALL_SCALE_VALUE = 0.75f;

    private MultiLineLabel emptyLabel;
    private MultiLineLabel phaseIdDescriptionLabel;
    private Tooltip trafficLightAreaTooltip;
    private GuiAreaDefinition trafficLightArea;
    private GuiAreaDefinition ctrlWindowArea;
    private GuiAreaDefinition ctrlButtonsArea;
    private GuiAreaDefinition ctrlSettingsArea;
    private GuiAreaDefinition[] trafficLightLightAreas;
    private final WidgetsCollection typeGroup = new WidgetsCollection();
    private final WidgetsCollection modelGroup = new WidgetsCollection();
    private final WidgetsCollection iconGroup = new WidgetsCollection();
    private final WidgetsCollection colorGroup = new WidgetsCollection();
    private final WidgetsCollection controlTypeGroup = new WidgetsCollection();
    private final Collection<Tooltip> iconTooltips = new ArrayList<>();
    private final Collection<Tooltip> colorTooltips = new ArrayList<>();
    private final Collection<Tooltip> modelTooltips = new ArrayList<>();
    private final Collection<Tooltip> controlTypeTooltips = new ArrayList<>();
    private final Map<TrafficLightControlType, WidgetsCollection> controlTypeTabGroups = new HashMap<>();
    private final Map<TrafficLightControlType, Collection<Tooltip>> controlTypeTabTooltips = new HashMap<>();
    
    private final Map<Byte, IconButton> indexedColorButtons = new HashMap<>();


    // mem
    private final BlockPos blockPos;
    private int selectedPart = -2;
    private int guiLeft;
    private int guiTop;


    // User settings
    private final Set<TrafficLightColor> enabledColors = new HashSet<>();
    private TrafficLightType type = TrafficLightType.CAR;
    private TrafficLightModel model = TrafficLightModel.THREE_LIGHTS;
    private TrafficLightIcon icon = TrafficLightIcon.NONE;
    private TrafficLightControlType controlType = TrafficLightControlType.STATIC;
    private TrafficLightColor[] colors = new TrafficLightColor[TrafficLightModel.maxRequiredSlots()];
    private int phaseId = 0;
    private boolean scheduleEnabled = true;

    // text    
    private static final Component textEmpty = GuiUtils.translate("gui.trafficcraft.trafficlight.empty");
    private static final Component textGeneralSettings = GuiUtils.translate("gui.trafficcraft.trafficlight.general_settings");
    private static final Component textSetSignal = GuiUtils.translate("gui.trafficcraft.trafficlight.set_signal");
    private static final Component textAreaTrafficLight = GuiUtils.translate("gui.trafficcraft.trafficlight.edit_traffic_light");
    private static final Component textCustomizeSchedule = GuiUtils.translate("gui.trafficcraft.trafficlight.edit_schedule");
    private static final Component textSetEnabledColors = GuiUtils.translate("gui.trafficcraft.trafficlight.set_enabled_colors");
    private static final Component textSetPhaseId = GuiUtils.translate("gui.trafficcraft.trafficlight.set_phase_id");
    private static final Component textPhaseIdDescription = GuiUtils.translate("gui.trafficcraft.trafficlight.set_phase_id.description");
    private static final String textStatus = GuiUtils.translate("gui.trafficcraft.trafficlight.schedule_status").getString();
    private static final String keyAreaTrafficLightSignal = "gui.trafficcraft.trafficlight.edit_signal_";

    public NewTrafficLightConfigScreen(Level level, BlockPos pos) {
        super(GuiUtils.translate("gui.trafficcraft.trafficlight.title"));
        this.blockPos = pos;

        Arrays.fill(colors, TrafficLightColor.NONE);
        for (TrafficLightControlType type : TrafficLightControlType.values()) {
            controlTypeTabTooltips.put(type, new ArrayList<>());
            controlTypeTabGroups.put(type, new WidgetsCollection());
        }

        if (level.getBlockState(pos).getBlock() instanceof TrafficLightBlock block) {            
            this.model = level.getBlockState(pos).getValue(TrafficLightBlock.MODEL);
        }
        if (level.getBlockEntity(pos) instanceof TrafficLightBlockEntity blockEntity) {
            for (TrafficLightColor color : blockEntity.getEnabledColors()) {
                this.enabledColors.add(color);
            }
            this.type = blockEntity.getTLType();
            this.icon = blockEntity.getIcon();
            this.controlType = blockEntity.getControlType();
            TrafficLightColor[] slots = blockEntity.getColorSlots();
            for (int i = 0; i < slots.length && i < this.colors.length; i++) {
                this.colors[i] = slots[i];
            }
            this.phaseId = blockEntity.getPhaseId();
            this.scheduleEnabled = blockEntity.isRunning();
        }
    }

    @Override
    public void onClose() {
        NetworkManager.getInstance().send(new TrafficLightPacket(blockPos, enabledColors, type, model, icon, controlType, colors, phaseId, scheduleEnabled), minecraft.player);
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = width / 2 - WINDOW_WIDTH / 2;
        guiTop = height / 2 - WINDOW_HEIGHT / 2;       
        
        typeGroup.clear();
        modelGroup.clear();
        iconGroup.clear();
        colorGroup.clear();

        /* GLOBAL SETTINGS WIDGETS */
        int typeButtonWidth = (WINDOW_WIDTH - WINDOW_PADDING_LEFT - INNER_PADDING * 2 - 2) / TrafficLightType.values().length;
        for (int i = 0; i < TrafficLightType.values().length; i++) {
            final TrafficLightType type = TrafficLightType.values()[i];
            final IconButton b = addRenderableWidget(new IconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                type.getSprite(),
                typeGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * typeButtonWidth,
                guiTop + 21 + INNER_TOP_PADDING,
                typeButtonWidth,
                18,
                GuiUtils.translate(type.getTranslationKey()),
                (btn) -> {
                    this.type = type;
                    initIconButtons();
                    initColorButtons();
                    for (int k = 0; k < colors.length; k++) {
                        TrafficLightColor color = colors[k];
                        if (color.isAllowedFor(type)) {
                            continue;
                        }
                        colors[k] = Arrays.stream(color.getSimilar()).filter(x -> x.isAllowedFor(type)).findFirst().orElse(TrafficLightColor.NONE);
                    }
                }
            ).withAlignment(Alignment.LEFT));

            addTooltip(Tooltip
                .of(List.of(GuiUtils.translate(type.getValueTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(type.getValueInfoTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.GRAY)))
                .withMaxWidth(width / 4)
                .assignedTo(b)
            );

            if (this.type == type) {
                b.select();
            }
        }

        for (int i = 0; i < TrafficLightModel.values().length; i++) {
            final TrafficLightModel model = TrafficLightModel.values()[i];
            final IconButton b = addRenderableWidget(new IconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                model.getSprite(),
                modelGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * IconButton.DEFAULT_BUTTON_WIDTH,
                guiTop + 21 + 25 + INNER_TOP_PADDING,
                null,
                (btn) -> {
                    this.model = model;
                    initModelButtonAreas();
                }
            ));

            addTooltip(Tooltip
                .of(List.of(GuiUtils.translate(model.getValueTranslationKey(ModMain.MOD_ID))))
                .withMaxWidth(width / 4)
                .assignedTo(b)
            );
            
            if (this.model == model) {
                b.select();
            }
        }
        
        emptyLabel = MultiLineLabel.create(this.font, textEmpty, WINDOW_WIDTH - WINDOW_PADDING_LEFT, 10);

        initIconButtons();
        initModelButtonAreas();
        initColorButtons();
        switchPartEditor(selectedPart);
    }

    private void initIconButtons() {
        iconGroup.performForEach(x -> removeWidget(x));
        iconGroup.clear();
        removeTooltips(x -> iconTooltips.contains(x));

        TrafficLightIcon[] icons = TrafficLightIcon.getAllowedForType(type);
        for (int i = 0; i < icons.length; i++) {
            final TrafficLightIcon icon = icons[i];
            final IconButton b = addRenderableWidget(new IconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                icon.getSprite(type),
                iconGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * IconButton.DEFAULT_BUTTON_WIDTH,
                guiTop + 21 + 50 + INNER_TOP_PADDING,
                null,
                (btn) -> {
                    this.icon = icon;
                    initColorButtons();
                }
            ));

            if (this.type == TrafficLightType.TRAM) {
                String signalName = "h-1";
                switch (icon) {
                    default:
                    case NONE:
                        signalName = "f5";
                        break;
                    case LEFT:
                        signalName = "f3";
                        break;
                    case RIGHT:
                        signalName = "f2";
                        break;
                    case STRAIGHT:
                        signalName = "f1";
                        break;
                }
                iconTooltips.add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(String.format("enum.%s.%s.%s", ModMain.MOD_ID, TrafficLightColor.NONE.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(String.format("enum.%s.%s.info.%s", ModMain.MOD_ID, TrafficLightColor.NONE.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {                
                iconTooltips.add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(icon.getValueTranslationKey(ModMain.MOD_ID))))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            }
            
            if (this.icon == icon) {
                b.select();
            }
        }

        iconGroup.setVisible(selectedPart == GLOBAL_SETTINGS_INDEX);
    }

    private void initColorButtons() {
        colorGroup.performForEach(x -> removeWidget(x));
        colorGroup.clear();
        indexedColorButtons.clear();
        removeTooltips(x -> colorTooltips.contains(x));

        TrafficLightColor[] colors = TrafficLightColor.getAllowedForType(type, true);
        for (int i = 0; i < colors.length; i++) {
            final TrafficLightColor color = colors[i];
            final byte j = color.getIndex();
            final IconButton b = addRenderableWidget(new IconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                new Sprite(TrafficLightTextureManager.getTextureLocation(new TrafficLightTextureKey(icon, color)), 16, 16, 0, 0, 16, 16),
                colorGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * IconButton.DEFAULT_BUTTON_WIDTH,
                guiTop,
                null,
                (btn) -> {
                    this.colors[selectedPart] = color;
                }
            ));
            
            if (color == TrafficLightColor.F1_F2_F3_F5) {
                String signalName = "h-1";
                switch (icon) {
                    case NONE:
                        signalName = "f5";
                        break;
                    case LEFT:
                        signalName = "f3";
                        break;
                    case RIGHT:
                        signalName = "f2";
                        break;
                    case STRAIGHT:
                        signalName = "f1";
                        break;
                    default:
                        break;
                }
                colorTooltips.add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(String.format("enum.%s.%s.%s", ModMain.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(String.format("enum.%s.%s.info.%s", ModMain.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {
                colorTooltips.add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(color.getValueTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(color.getValueInfoTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            }
            
            indexedColorButtons.put(j, b);        
        }
        
        colorGroup.setVisible(selectedPart >= 0);
        
        initControlTypeStatic();
    }

    private void initModelButtonAreas() {
        removeTooltips(x -> modelTooltips.contains(x));

        trafficLightArea = new GuiAreaDefinition(guiLeft - 5, guiTop + 20 - 5, 48 + 10, (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10);      
        trafficLightAreaTooltip = Tooltip
            .of(List.of(textAreaTrafficLight))
            .withMaxWidth(width / 4)
            .assignedTo(trafficLightArea)
        ;
        modelTooltips.add(trafficLightAreaTooltip);

        trafficLightLightAreas = new GuiAreaDefinition[Math.min(colors.length, model.getLightsCount())];

        for (int i = 0; i < trafficLightLightAreas.length; i++) {
            trafficLightLightAreas[i] = new GuiAreaDefinition((int)(12 + guiLeft), (int)(9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * i + guiTop + 20), TRAFFIC_LIGHT_LIGHT_SIZE, TRAFFIC_LIGHT_LIGHT_SIZE);
            
            modelTooltips.add(addTooltip(Tooltip
                .of(List.of(GuiUtils.translate(keyAreaTrafficLightSignal + i)))
                .withMaxWidth(width / 4)
                .assignedTo(trafficLightLightAreas[i])
            ));
        }

        initControlTypeAreas();
    }

    private void initControlTypeAreas() {
        ctrlWindowArea = new GuiAreaDefinition(guiLeft, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10, WINDOW_WIDTH, 100);
        ctrlButtonsArea = new GuiAreaDefinition(guiLeft + INNER_PADDING, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10 + INNER_TOP_PADDING, WINDOW_WIDTH - INNER_PADDING * 2, IconButton.DEFAULT_BUTTON_HEIGHT + 2);
        ctrlSettingsArea = new GuiAreaDefinition(guiLeft + INNER_PADDING, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10 + INNER_TOP_PADDING + IconButton.DEFAULT_BUTTON_HEIGHT + 2, WINDOW_WIDTH - INNER_PADDING * 2, 53);

        removeTooltips(x -> controlTypeTooltips.contains(x));
        controlTypeGroup.clear(x -> removeWidget(x));

        controlTypeTabTooltips.values().forEach(x -> {
            removeTooltips(y -> x.contains(y));
            x.clear();
        });
        controlTypeTabGroups.values().forEach(x -> x.clear(y -> removeWidget(y)));

        // Common buttons
        final int ctrlBtnW = ctrlButtonsArea.getWidth() - 2;
        final ItemButton bt = addRenderableWidget(new ItemButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            controlType.getIconStack(),
            controlTypeGroup,
            ctrlButtonsArea.getLeft() + 1,
            ctrlButtonsArea.getTop() + 1,
            ctrlBtnW,                
            ctrlButtonsArea.getHeight() - 2,
            GuiUtils.translate(controlType.getValueTranslationKey(ModMain.MOD_ID)),
            (btn) -> {
                ItemButton ibtn = (ItemButton)btn;
                controlType = controlType.next();
                switchControlType(controlType);
                ibtn.withItem(controlType.getIconStack());
                btn.setMessage(GuiUtils.translate(controlType.getValueTranslationKey(ModMain.MOD_ID)));
            }
        ).withAlignment(Alignment.LEFT).withDefaultItemTooltip(false));

        controlTypeTooltips.add(addTooltip(Tooltip
            .of(GuiUtils.getEnumTooltipData(ModMain.MOD_ID, TrafficLightControlType.class))
            .withMaxWidth(width / 4)
            .assignedTo(bt)
        ));

        // tabs
        initControlTypeStatic();
        initControlTypeOwnSchedule();
        initControlTypeRemote();
    }

    private void initControlTypeStatic() {
        removeTooltips(y -> controlTypeTabTooltips.get(TrafficLightControlType.STATIC).contains(y));
        controlTypeTabTooltips.get(TrafficLightControlType.STATIC).clear();
        controlTypeTabGroups.get(TrafficLightControlType.STATIC).clear(y -> removeWidget(y));

        TrafficLightColor[] colors = TrafficLightColor.getAllowedForType(type, false);
        for (int i = 0; i < colors.length; i++) {
            final TrafficLightColor color = colors[i];
            final IconButton b = addRenderableWidget(new IconButton(
                ButtonType.TOGGLE_BUTTON,
                AreaStyle.BROWN,
                new Sprite(TrafficLightTextureManager.getTextureLocation(new TrafficLightTextureKey(icon, color)), 16, 16, 0, 0, 16, 16),
                controlTypeTabGroups.get(TrafficLightControlType.STATIC),
                ctrlSettingsArea.getRight() - 1 - (colors.length - i) * IconButton.DEFAULT_BUTTON_WIDTH,
                ctrlSettingsArea.getTop() + 1,
                null,
                (btn) -> {
                    IconButton ibtn = (IconButton)btn;
                    if (ibtn.isSelected()) {
                        enabledColors.add(color);
                    } else { 
                        enabledColors.removeIf(x -> x == color);
                    }
                }
            ) {
                @Override
                public void renderImage(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                    GuiUtils.setShaderColor(1, 1, 1, 0.5f);
                    super.renderImage(pPoseStack, pMouseX, pMouseY, pPartialTick);
                    GuiUtils.setShaderColor(1, 1, 1, 1);
                }
            });
            
            if (color == TrafficLightColor.F1_F2_F3_F5) {
                String signalName = "h-1";
                switch (icon) {
                    case NONE:
                        signalName = "f5";
                        break;
                    case LEFT:
                        signalName = "f3";
                        break;
                    case RIGHT:
                        signalName = "f2";
                        break;
                    case STRAIGHT:
                        signalName = "f1";
                        break;
                    default:
                        break;
                }
                controlTypeTabTooltips.get(TrafficLightControlType.STATIC).add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(String.format("enum.%s.%s.%s", ModMain.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(String.format("enum.%s.%s.info.%s", ModMain.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {
                controlTypeTabTooltips.get(TrafficLightControlType.STATIC).add(addTooltip(Tooltip
                    .of(List.of(GuiUtils.translate(color.getValueTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.BOLD), GuiUtils.translate(color.getValueInfoTranslationKey(ModMain.MOD_ID)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            }

            if (this.enabledColors.contains(color)) {
                b.select();
            }
        }
        controlTypeTabGroups.get(TrafficLightControlType.STATIC).setVisible(controlType == TrafficLightControlType.STATIC);
    }

    private void initControlTypeOwnSchedule() {
        removeTooltips(y -> controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).contains(y));
        controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).clear();
        controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE).clear(y -> removeWidget(y));
        addRenderableWidget(new IconButton(
            ButtonType.DEFAULT, 
            AreaStyle.GRAY, 
            Sprite.empty(),
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            ctrlSettingsArea.getLeft() + 1,
            ctrlSettingsArea.getTop() + 1,
            ctrlSettingsArea.getWidth() - 2,
            IconButton.DEFAULT_BUTTON_HEIGHT,
            textCustomizeSchedule,
            (btn) -> {
                Minecraft.getInstance().setScreen(new NewTrafficLightScheduleEditor(this));
            }
        ));
        addRenderableWidget(new IconButton(
            ButtonType.DEFAULT, 
            AreaStyle.GRAY, 
            Sprite.empty(),
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            ctrlSettingsArea.getLeft() + 1,
            ctrlSettingsArea.getTop() + 1 + IconButton.DEFAULT_BUTTON_HEIGHT,
            ctrlSettingsArea.getWidth() - 2,
            IconButton.DEFAULT_BUTTON_HEIGHT,
            GuiUtils.text(textStatus + ": " + (scheduleEnabled ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())),
            (btn) -> {
                scheduleEnabled = !scheduleEnabled;
                btn.setMessage(GuiUtils.text(textStatus + ": " + (scheduleEnabled ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())));
            }
        ));
        controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE).setVisible(controlType == TrafficLightControlType.OWN_SCHEDULE);
    }

    private void initControlTypeRemote() {
        removeTooltips(y -> controlTypeTabTooltips.get(TrafficLightControlType.REMOTE).contains(y));
        controlTypeTabTooltips.get(TrafficLightControlType.REMOTE).clear();
        controlTypeTabGroups.get(TrafficLightControlType.REMOTE).clear(y -> removeWidget(y));
        EditBox box = addEditBox(
            ctrlSettingsArea.getRight() - 2 - 50,
            ctrlSettingsArea.getTop() + 2,
            50,
            IconButton.DEFAULT_BUTTON_HEIGHT - 2,
            String.valueOf(phaseId),
            true,
            (text) -> {
                try {
                    phaseId = Integer.parseInt(text);
                } catch (Exception e) {}
            },
            NO_EDIT_BOX_FOCUS_CHANGE_ACTION,
            null
        );
        box.setFilter(GuiUtils::editBoxNumberFilter);
        box.setMaxLength(3);
        controlTypeTabGroups.get(TrafficLightControlType.REMOTE).add(box);
        controlTypeTabGroups.get(TrafficLightControlType.REMOTE).setVisible(controlType == TrafficLightControlType.REMOTE);

        phaseIdDescriptionLabel = MultiLineLabel.create(this.font, textPhaseIdDescription, (int)((ctrlSettingsArea.getWidth() - 8) / SMALL_SCALE_VALUE), 10);
    }

    private void switchPartEditor(int partIndex) {
        this.selectedPart = partIndex;

        typeGroup.setVisible(false);
        modelGroup.setVisible(false);
        iconGroup.setVisible(false);
        colorGroup.setVisible(false);

        if (selectedPart == GLOBAL_SETTINGS_INDEX) {
            typeGroup.setVisible(true);
            modelGroup.setVisible(true);
            iconGroup.setVisible(true);
        } else if (selectedPart >= 0) {
            colorGroup.setVisible(true);
            if (selectedPart < colors.length && indexedColorButtons.containsKey(colors[selectedPart].getIndex())) {
                colorGroup.performForEachOfType(IconButton.class, x -> x.deselect());
                indexedColorButtons.get(colors[selectedPart].getIndex()).select();
            }
        } else {
            
        }
    }

    private void switchControlType(TrafficLightControlType controlType) {
        this.controlType = controlType;

        controlTypeTabGroups.entrySet().stream().filter(x -> x.getValue() instanceof WidgetsCollection).forEach(x -> ((WidgetsCollection)x.getValue()).setVisible(x.getKey() == controlType));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (int i = 0; i < trafficLightLightAreas.length; i++) {
            if (trafficLightLightAreas[i].isInBounds(pMouseX, pMouseY)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                switchPartEditor(i);
                return true;
            }
        }

        if (trafficLightArea.isInBounds(pMouseX, pMouseY)) {
            switchPartEditor(GLOBAL_SETTINGS_INDEX);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void renderBg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBg(pPoseStack, pMouseX, pMouseY, pPartialTick);
        Lighting.setupForFlatItems();
        this.renderBackground(pPoseStack);
        //DynamicGuiRenderer.renderArea(pPoseStack, guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT, AreaStyle.GRAY, ButtonState.BUTTON);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, guiTop, 16777215);

        // Render traffic light
        pPoseStack.pushPose();
        pPoseStack.setIdentity();
        pPoseStack.translate((double)guiLeft + 72, guiTop + 116, -100);
        pPoseStack.scale(96, 96, -96);
        pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ModBlocks.TRAFFIC_LIGHT.get().defaultBlockState().setValue(TrafficLightBlock.MODEL, model), pPoseStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        pPoseStack.popPose();
        multibuffersource$buffersource.endBatch();
        pPoseStack.popPose();
        Lighting.setupFor3DItems();

        // render lights
        for (int i = 0; i < colors.length && i < model.getLightsCount(); i++) {
            GuiUtils.blit(TrafficLightTextureManager.getTextureLocation(new TrafficLightTextureKey(icon, colors[i])), pPoseStack, (int)(12 + guiLeft), (int)(9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * i + guiTop + 20), TRAFFIC_LIGHT_LIGHT_SIZE, TRAFFIC_LIGHT_LIGHT_SIZE, 0, 0, 16, 16, 16, 16);
        }        

        // render settings pannels
        if (selectedPart == GLOBAL_SETTINGS_INDEX) {
            renderGlobalWindow(pPoseStack, pMouseX, pMouseY, pPartialTick);
        } else if (selectedPart >= 0) {
            renderPartEditor(pPoseStack, pMouseX, pMouseY, pPartialTick);
        } else {
            renderEmptyWindow(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        // render hover outline
        GuiAreaDefinition lightDef = Arrays.stream(trafficLightLightAreas).filter(x -> x.isInBounds(pMouseX, pMouseY)).findFirst().orElse(null);
        if (lightDef != null) {
            GuiUtils.renderBoundingBox(pPoseStack, lightDef, 0x55FFFFFF, 0xFFFFFFFF);
        } else if (trafficLightArea.isInBounds(pMouseX, pMouseY)) {
            GuiUtils.renderBoundingBox(pPoseStack, trafficLightArea, 0x55FFFFFF, 0xFFFFFFFF);
        }

        // render controlling window
        DynamicGuiRenderer.renderWindow(pPoseStack, ctrlWindowArea.getLeft(), ctrlWindowArea.getTop(), ctrlWindowArea.getWidth(), ctrlWindowArea.getHeight());
        DynamicGuiRenderer.renderArea(pPoseStack, ctrlButtonsArea.getLeft(), ctrlButtonsArea.getTop(), ctrlButtonsArea.getWidth(), ctrlButtonsArea.getHeight(), AreaStyle.GRAY, ButtonState.SUNKEN);
        DynamicGuiRenderer.renderContainerBackground(pPoseStack, ctrlSettingsArea.getLeft(), ctrlSettingsArea.getTop(), ctrlSettingsArea.getWidth(), ctrlSettingsArea.getHeight());
        font.draw(pPoseStack, GuiUtils.translate(TrafficLightControlType.STATIC.getEnumTranslationKey(ModMain.MOD_ID)), ctrlWindowArea.getLeft() + INNER_PADDING, ctrlWindowArea.getTop() + 7, DragonLibConstants.DEFAULT_UI_FONT_COLOR);

        // render controltype tab
        switch (controlType) {
            case STATIC:
                renderControlTypeStatic(pPoseStack, pMouseX, pMouseY, pPartialTick);
                break;
            case REMOTE:
                renderControlTypeRemote(pPoseStack, pMouseX, pMouseY, pPartialTick);
                break;
            default:
                break;
        }
    }

    @Override
    public void renderFg(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderFg(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (Arrays.stream(trafficLightLightAreas).filter(x -> x.isInBounds(pMouseX, pMouseY)).findFirst().orElse(null) == null && trafficLightArea.isInBounds(pMouseX, pMouseY)) {
            trafficLightAreaTooltip.render(this, pPoseStack, pMouseX, pMouseY);
        }
    }

    public void renderEmptyWindow(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        emptyLabel.renderCentered(pPoseStack, guiLeft + WINDOW_PADDING_LEFT + (WINDOW_WIDTH - WINDOW_PADDING_LEFT) / 2, guiTop + 20 + 96 / 2 - emptyLabel.getLineCount() * 5, 10, 0xDBDBDB);
    }   

    public void renderGlobalWindow(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderWindow(pPoseStack, guiLeft + WINDOW_PADDING_LEFT, guiTop + 20, WINDOW_WIDTH - WINDOW_PADDING_LEFT, 96);
        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, guiLeft + WINDOW_PADDING_LEFT - 9, guiTop + 20 + 96 / 2 - 9, 0, 0, 12, 18, 32, 32);

        DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING, WINDOW_WIDTH - WINDOW_PADDING_LEFT - INNER_PADDING * 2, 20, AreaStyle.GRAY, ButtonState.SUNKEN);
        DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING + 25, TrafficLightModel.values().length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.SUNKEN);
        DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING + 50, TrafficLightIcon.getAllowedForType(type).length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.SUNKEN);
        
        font.draw(pPoseStack, textGeneralSettings, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 27, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
    }   
    
    public void renderPartEditor(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int windowHeight = INNER_TOP_PADDING + 27;
        int yBase = guiTop + 20;
        int y = 9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * selectedPart + yBase + TRAFFIC_LIGHT_LIGHT_SIZE / 2 - windowHeight / 2;

        DynamicGuiRenderer.renderWindow(pPoseStack, guiLeft + WINDOW_PADDING_LEFT, y, TrafficLightColor.getAllowedForType(type, true).length * 18 + 2 + INNER_PADDING * 2, windowHeight);
        GuiUtils.blit(WIDGETS_LOCATION, pPoseStack, guiLeft + WINDOW_PADDING_LEFT - 9, y + windowHeight / 2 - 9, 0, 0, 12, 18, 32, 32);

        colorGroup.performForEach(x -> {
            x.y = y + INNER_TOP_PADDING + 1;
        });
        DynamicGuiRenderer.renderArea(pPoseStack, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, y + INNER_TOP_PADDING, TrafficLightColor.getAllowedForType(type, true).length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.SUNKEN);

        font.draw(pPoseStack, textSetSignal, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, y + 7, DragonLibConstants.DEFAULT_UI_FONT_COLOR);
    } 

    public void renderControlTypeStatic(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        font.draw(pPoseStack, textSetEnabledColors, ctrlSettingsArea.getLeft() + 4, ctrlSettingsArea.getTop() + IconButton.DEFAULT_BUTTON_HEIGHT / 2 - font.lineHeight / 2, 0xDBDBDB);
    }

    public void renderControlTypeRemote(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        font.draw(pPoseStack, textSetPhaseId, ctrlSettingsArea.getLeft() + 4, ctrlSettingsArea.getTop() + IconButton.DEFAULT_BUTTON_HEIGHT / 2 - font.lineHeight / 2, 0xDBDBDB);
        
        float scale = 0.75f;
        pPoseStack.scale(scale, scale, scale);
        phaseIdDescriptionLabel.renderLeftAlignedNoShadow(pPoseStack, (int)((ctrlSettingsArea.getLeft() + 4) / SMALL_SCALE_VALUE), (int)((ctrlSettingsArea.getTop() + IconButton.DEFAULT_BUTTON_HEIGHT + 10) / SMALL_SCALE_VALUE), 10, 0xCBCBCB);
        pPoseStack.setIdentity();
    }
}
