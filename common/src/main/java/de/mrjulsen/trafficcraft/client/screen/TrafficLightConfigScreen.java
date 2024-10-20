package de.mrjulsen.trafficcraft.client.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.data.Clipboard;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.Wikipedia;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.ModGuiUtils;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.client.ModGuiUtils.HelpButtonComponents;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager.TrafficLightTextureKey;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightSchedulePacket;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

public class TrafficLightConfigScreen extends DLScreen {

    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/window_arrow.png");
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
    private DLTooltip trafficLightAreaTooltip;
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
    private final Collection<DLTooltip> iconTooltips = new ArrayList<>();
    private final Collection<DLTooltip> colorTooltips = new ArrayList<>();
    private final Collection<DLTooltip> modelTooltips = new ArrayList<>();
    private final Collection<DLTooltip> controlTypeTooltips = new ArrayList<>();
    private final Map<TrafficLightControlType, WidgetsCollection> controlTypeTabGroups = new HashMap<>();
    private final Map<TrafficLightControlType, Collection<DLTooltip>> controlTypeTabTooltips = new HashMap<>();
    
    private final Map<Byte, DLIconButton> indexedColorButtons = new HashMap<>();


    // mem
    private final Level level;
    private final BlockPos blockPos;
    private int selectedPart = -2;
    private int guiLeft;
    private int guiTop;
    private DLIconButton pasteButton;


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
    private static final Component textEmpty = TextUtils.translate("gui.trafficcraft.trafficlight.empty");
    private static final Component textGeneralSettings = TextUtils.translate("gui.trafficcraft.trafficlight.general_settings");
    private static final Component textSetSignal = TextUtils.translate("gui.trafficcraft.trafficlight.set_signal");
    private static final Component textAreaTrafficLight = TextUtils.translate("gui.trafficcraft.trafficlight.edit_traffic_light");
    private static final Component textCustomizeSchedule = TextUtils.translate("gui.trafficcraft.trafficlight.edit_schedule");
    private static final Component textSetEnabledColors = TextUtils.translate("gui.trafficcraft.trafficlight.set_enabled_colors");
    private static final Component textSetPhaseId = TextUtils.translate("gui.trafficcraft.trafficlight.set_phase_id");
    private static final Component textPhaseIdDescription = TextUtils.translate("gui.trafficcraft.trafficlight.set_phase_id.description");
    private static final String textStatus = TextUtils.translate("gui.trafficcraft.trafficlight.schedule_status").getString();
    private static final String keyAreaTrafficLightSignal = "gui.trafficcraft.trafficlight.edit_signal_";
    
    private static final String keyhelpTrafficLight = "gui.trafficcraft.trafficlight.help.traffic_light";
    private static final String keyhelpTrafficLightDescription = "gui.trafficcraft.trafficlight.helpdesc.traffic_light";    
    private static final String keyhelpTramTrafficLight = "gui.trafficcraft.trafficlight.help.traffic_light_tram";
    private static final String keyhelpTramTrafficLightDescription = "gui.trafficcraft.trafficlight.helpdesc.traffic_light_tram";

    public TrafficLightConfigScreen(Level level, BlockPos pos) {
        super(TextUtils.translate("gui.trafficcraft.trafficlight.title"));
        this.level = level;
        this.blockPos = pos;

        Arrays.fill(colors, TrafficLightColor.NONE);
        for (TrafficLightControlType type : TrafficLightControlType.values()) {
            controlTypeTabTooltips.put(type, new ArrayList<>());
            controlTypeTabGroups.put(type, new WidgetsCollection());
        }

        if (level.getBlockState(pos).getBlock() instanceof TrafficLightBlock) {            
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
        TrafficCraft.net().sendToServer(new TrafficLightPacket(blockPos, enabledColors, type, model, icon, controlType, colors, phaseId, scheduleEnabled));
        super.onClose();
    }

    @Override
    public void tick() {
        super.tick();

        if (pasteButton != null) {
            pasteButton.active = Clipboard.contains(TrafficLightSchedule.class);
        }
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
            final DLIconButton b = addRenderableWidget(new DLIconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                type.getSprite(),
                typeGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * typeButtonWidth,
                guiTop + 21 + INNER_TOP_PADDING,
                typeButtonWidth,
                18,
                TextUtils.translate(type.getTranslationKey()),
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
            ).withAlignment(EAlignment.LEFT));

            addTooltip(DLTooltip
                .of(List.of(TextUtils.translate(type.getValueTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.BOLD), TextUtils.translate(type.getValueInfoTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.GRAY)))
                .withMaxWidth(width / 4)
                .assignedTo(b)
            );

            if (this.type == type) {
                b.select();
            }
        }

        for (int i = 0; i < TrafficLightModel.values().length; i++) {
            final TrafficLightModel model = TrafficLightModel.values()[i];
            final DLIconButton b = addRenderableWidget(new DLIconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                model.getSprite(),
                modelGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * DLIconButton.DEFAULT_BUTTON_WIDTH,
                guiTop + 21 + 25 + INNER_TOP_PADDING,
                null,
                (btn) -> {
                    this.model = model;
                    initModelButtonAreas();
                }
            ));

            addTooltip(DLTooltip
                .of(List.of(TextUtils.translate(model.getValueTranslationKey(TrafficCraft.MOD_ID))))
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
            final DLIconButton b = addRenderableWidget(new DLIconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                icon.getSprite(type),
                iconGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * DLIconButton.DEFAULT_BUTTON_WIDTH,
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
                iconTooltips.add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(String.format("enum.%s.%s.%s", TrafficCraft.MOD_ID, TrafficLightColor.NONE.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), TextUtils.translate(String.format("enum.%s.%s.info.%s", TrafficCraft.MOD_ID, TrafficLightColor.NONE.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {                
                iconTooltips.add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(icon.getValueTranslationKey(TrafficCraft.MOD_ID))))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            }
            
            if (this.icon == icon) {
                b.select();
            }
        }

        String url = Constants.WIKIPEDIA_TRAFFIC_LIGHT_ID;
        Component helpTitle = TextUtils.empty();
        Collection<Component> helpDescription = new ArrayList<>();

        if (type == TrafficLightType.TRAM && Locale.getDefault().getLanguage().equals(Constants.GERMAN_LOCAL_CODE)) { // For german players only because that article only exists in germany
            url = Constants.WIKIPEDIA_GERMAN_TRAM_SIGNAL_ID;
            helpTitle = TextUtils.translate(keyhelpTramTrafficLight).withStyle(ChatFormatting.BOLD);
            helpDescription.add(TextUtils.translate(keyhelpTramTrafficLightDescription).withStyle(ChatFormatting.GRAY));
        } else {    
            url = Constants.WIKIPEDIA_TRAFFIC_LIGHT_ID;
            helpTitle = TextUtils.translate(keyhelpTrafficLight).withStyle(ChatFormatting.BOLD);
            helpDescription.add(TextUtils.translate(keyhelpTrafficLightDescription).withStyle(ChatFormatting.GRAY));
        }

        HelpButtonComponents data = ModGuiUtils.createHelpButton(
            this,
            guiLeft + WINDOW_WIDTH + 4,
            guiTop + 20,
            DLIconButton.DEFAULT_BUTTON_WIDTH,
            DLIconButton.DEFAULT_BUTTON_HEIGHT,
            iconGroup,
            AreaStyle.NATIVE,
            Wikipedia.getArticle(url).getArticleUrl(Locale.getDefault().getLanguage()),
            helpTitle,
            helpDescription
        );
        addRenderableWidget(data.helpButton());
        iconTooltips.add(addTooltip(data.tooltip()));

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
            final DLIconButton b = addRenderableWidget(new DLIconButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                new Sprite(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(icon, color)), 16, 16, 0, 0, 16, 16),
                colorGroup,
                guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING + 1 + i * DLIconButton.DEFAULT_BUTTON_WIDTH,
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
                colorTooltips.add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(String.format("enum.%s.%s.%s", TrafficCraft.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), TextUtils.translate(String.format("enum.%s.%s.info.%s", TrafficCraft.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {
                colorTooltips.add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(color.getValueTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.BOLD), TextUtils.translate(color.getValueInfoTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.GRAY)))
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
        trafficLightAreaTooltip = DLTooltip
            .of(List.of(textAreaTrafficLight))
            .withMaxWidth(width / 4)
            .assignedTo(trafficLightArea)
        ;
        modelTooltips.add(trafficLightAreaTooltip);

        trafficLightLightAreas = new GuiAreaDefinition[Math.min(colors.length, model.getLightsCount())];

        for (int i = 0; i < trafficLightLightAreas.length; i++) {
            trafficLightLightAreas[i] = new GuiAreaDefinition((int)(12 + guiLeft), (int)(9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * i + guiTop + 20), TRAFFIC_LIGHT_LIGHT_SIZE, TRAFFIC_LIGHT_LIGHT_SIZE);
            
            modelTooltips.add(addTooltip(DLTooltip
                .of(List.of(TextUtils.translate(keyAreaTrafficLightSignal + i)))
                .withMaxWidth(width / 4)
                .assignedTo(trafficLightLightAreas[i])
            ));
        }

        initControlTypeAreas();
    }

    private void initControlTypeAreas() {
        ctrlWindowArea = new GuiAreaDefinition(guiLeft, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10, WINDOW_WIDTH, 100);
        ctrlButtonsArea = new GuiAreaDefinition(guiLeft + INNER_PADDING, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10 + INNER_TOP_PADDING, WINDOW_WIDTH - INNER_PADDING * 2, DLIconButton.DEFAULT_BUTTON_HEIGHT + 2);
        ctrlSettingsArea = new GuiAreaDefinition(guiLeft + INNER_PADDING, guiTop + 20 + (int)(Math.max(model.getTotalHitboxHeight(), 16.0f) * 6.0f) + 10 + INNER_TOP_PADDING + DLIconButton.DEFAULT_BUTTON_HEIGHT + 2, WINDOW_WIDTH - INNER_PADDING * 2, 53);

        removeTooltips(x -> controlTypeTooltips.contains(x));
        controlTypeGroup.clear(x -> removeWidget(x));

        controlTypeTabTooltips.values().forEach(x -> {
            removeTooltips(y -> x.contains(y));
            x.clear();
        });
        controlTypeTabGroups.values().forEach(x -> x.clear(y -> removeWidget(y)));

        // Common buttons
        final int ctrlBtnW = ctrlButtonsArea.getWidth() - 2;
        final DLItemButton bt = addRenderableWidget(new DLItemButton(
            ButtonType.DEFAULT,
            AreaStyle.BROWN,
            controlType.getIconStack(),
            controlTypeGroup,
            ctrlButtonsArea.getLeft() + 1,
            ctrlButtonsArea.getTop() + 1,
            ctrlBtnW,                
            ctrlButtonsArea.getHeight() - 2,
            TextUtils.translate(controlType.getValueTranslationKey(TrafficCraft.MOD_ID)),
            (btn) -> {
                DLItemButton ibtn = (DLItemButton)btn;
                controlType = controlType.next();
                switchControlType(controlType);
                ibtn.withItem(controlType.getIconStack());
                btn.setMessage(TextUtils.translate(controlType.getValueTranslationKey(TrafficCraft.MOD_ID)));
            }
        ).withAlignment(EAlignment.LEFT).withDefaultItemTooltip(false));

        controlTypeTooltips.add(addTooltip(DLTooltip
            .of(TrafficCraft.MOD_ID, TrafficLightControlType.class)
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
            final DLIconButton b = addRenderableWidget(new DLIconButton(
                ButtonType.TOGGLE_BUTTON,
                AreaStyle.BROWN,
                new Sprite(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(icon, color)), 16, 16, 0, 0, 16, 16),
                controlTypeTabGroups.get(TrafficLightControlType.STATIC),
                ctrlSettingsArea.getRight() - 1 - (colors.length - i) * DLIconButton.DEFAULT_BUTTON_WIDTH,
                ctrlSettingsArea.getTop() + 1,
                null,
                (btn) -> {                    
                    if (btn.isSelected()) {
                        enabledColors.add(color);
                    } else { 
                        enabledColors.removeIf(x -> x == color);
                    }
                }
            ) {
                @Override
                public void renderImage(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
                    GuiUtils.setTint(1, 1, 1, 0.5f);
                    super.renderImage(graphics, pMouseX, pMouseY, pPartialTick);
                    GuiUtils.resetTint();
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
                controlTypeTabTooltips.get(TrafficLightControlType.STATIC).add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(String.format("enum.%s.%s.%s", TrafficCraft.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.BOLD), TextUtils.translate(String.format("enum.%s.%s.info.%s", TrafficCraft.MOD_ID, color.getEnumName(), signalName)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            } else {
                controlTypeTabTooltips.get(TrafficLightControlType.STATIC).add(addTooltip(DLTooltip
                    .of(List.of(TextUtils.translate(color.getValueTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.BOLD), TextUtils.translate(color.getValueInfoTranslationKey(TrafficCraft.MOD_ID)).withStyle(ChatFormatting.GRAY)))
                    .withMaxWidth(width / 4)
                    .assignedTo(b)
                ));
            }

            if (this.enabledColors.stream().anyMatch(x -> x.isSimilar(color))) {
                b.select();
            }
        }
        controlTypeTabGroups.get(TrafficLightControlType.STATIC).setVisible(controlType == TrafficLightControlType.STATIC);
    }

    private void initControlTypeOwnSchedule() {
        removeTooltips(y -> controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).contains(y));
        controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).clear();
        controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE).clear(y -> removeWidget(y));
        // editor
        addRenderableWidget(new DLIconButton(
            ButtonType.DEFAULT, 
            AreaStyle.GRAY, 
            Sprite.empty(),
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            ctrlSettingsArea.getLeft() + 1,
            ctrlSettingsArea.getTop() + 1,
            ctrlSettingsArea.getWidth() - 2 - DLIconButton.DEFAULT_BUTTON_HEIGHT * 2,
            DLIconButton.DEFAULT_BUTTON_HEIGHT,
            textCustomizeSchedule,
            (btn) -> {
                DLScreen.setScreen(new TrafficLightScheduleEditor(this, level, blockPos));
            }
        ));
        // copy
        DLIconButton copyBtn = addRenderableWidget(ModGuiUtils.createCopyButton(
            ctrlSettingsArea.getLeft() + 1 + ctrlSettingsArea.getWidth() - 2 - DLIconButton.DEFAULT_BUTTON_HEIGHT * 2,
            ctrlSettingsArea.getTop() + 1,
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            AreaStyle.GRAY,
            (btn) -> {
                if (level.getBlockEntity(blockPos) instanceof TrafficLightBlockEntity blockEntity) {
                    Clipboard.put(TrafficLightSchedule.class, blockEntity.getSchedule());
                }
            })
        );
        controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).add(addTooltip(DLTooltip.of(Constants.textCopy).assignedTo(copyBtn)));
        // paste
        pasteButton = addRenderableWidget(ModGuiUtils.createPasteButton(
            ctrlSettingsArea.getLeft() + 1 + ctrlSettingsArea.getWidth() - 2 - DLIconButton.DEFAULT_BUTTON_HEIGHT,
            ctrlSettingsArea.getTop() + 1,
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            AreaStyle.GRAY,
            (btn) ->  {
                Optional<TrafficLightSchedule> schedule = Clipboard.get(TrafficLightSchedule.class);
                if (schedule.isPresent()) {
                    if (schedule.get() != null) {
                        TrafficCraft.net().sendToServer(new TrafficLightSchedulePacket(
                            blockPos,
                            List.of(schedule.get())
                        ));
                    }
                }
            })
        );
        pasteButton.active = false;
        controlTypeTabTooltips.get(TrafficLightControlType.OWN_SCHEDULE).add(addTooltip(DLTooltip.of(Constants.textPaste).assignedTo(pasteButton)));
        // status
        addRenderableWidget(new DLIconButton(
            ButtonType.DEFAULT, 
            AreaStyle.GRAY, 
            Sprite.empty(),
            controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE),
            ctrlSettingsArea.getLeft() + 1,
            ctrlSettingsArea.getTop() + 1 + DLIconButton.DEFAULT_BUTTON_HEIGHT,
            ctrlSettingsArea.getWidth() - 2,
            DLIconButton.DEFAULT_BUTTON_HEIGHT,
            TextUtils.text(textStatus + ": " + (scheduleEnabled ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())),
            (btn) -> {
                scheduleEnabled = !scheduleEnabled;
                btn.setMessage(TextUtils.text(textStatus + ": " + (scheduleEnabled ? CommonComponents.OPTION_ON.getString() : CommonComponents.OPTION_OFF.getString())));
            }
        ));
        controlTypeTabGroups.get(TrafficLightControlType.OWN_SCHEDULE).setVisible(controlType == TrafficLightControlType.OWN_SCHEDULE);
    }

    private void initControlTypeRemote() {
        removeTooltips(y -> controlTypeTabTooltips.get(TrafficLightControlType.REMOTE).contains(y));
        controlTypeTabTooltips.get(TrafficLightControlType.REMOTE).clear();
        controlTypeTabGroups.get(TrafficLightControlType.REMOTE).clear(y -> removeWidget(y));
        DLEditBox box = addEditBox(
            ctrlSettingsArea.getRight() - 2 - 50,
            ctrlSettingsArea.getTop() + 2,
            50,
            DLIconButton.DEFAULT_BUTTON_HEIGHT - 2,
            String.valueOf(phaseId),
            TextUtils.empty(),
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
        box.setMaxLength(4);
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
                colorGroup.performForEachOfType(DLIconButton.class, x -> x.deselect());
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
    public void renderMainLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Lighting.setupForFlatItems();
        renderScreenBackground(graphics);
        GuiUtils.drawString(graphics, font, this.width / 2, guiTop, title, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.CENTER, false);

        // Render traffic light
        graphics.poseStack().pushPose();
        graphics.poseStack().setIdentity();
        graphics.poseStack().translate((double)guiLeft + 72, guiTop + 116, -100);
        graphics.poseStack().scale(96, 96, -96);
        graphics.poseStack().mulPose(Vector3f.ZP.rotationDegrees(180));
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ModBlocks.TRAFFIC_LIGHT.get().defaultBlockState().setValue(TrafficLightBlock.MODEL, model), graphics.poseStack(), multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY);
        multibuffersource$buffersource.endBatch();
        graphics.poseStack().popPose();
        Lighting.setupFor3DItems();

        // render lights
        for (int i = 0; i < colors.length && i < model.getLightsCount(); i++) {
            GuiUtils.drawTexture(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(icon, colors[i])), graphics, (int)(12 + guiLeft), (int)(9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * i + guiTop + 20), TRAFFIC_LIGHT_LIGHT_SIZE, TRAFFIC_LIGHT_LIGHT_SIZE, 0, 0, 16, 16, 16, 16);
        }        

        // render settings pannels
        if (selectedPart == GLOBAL_SETTINGS_INDEX) {
            renderGlobalWindow(graphics, pMouseX, pMouseY, pPartialTick);
        } else if (selectedPart >= 0) {
            renderPartEditor(graphics, pMouseX, pMouseY, pPartialTick);
        } else {
            renderEmptyWindow(graphics, pMouseX, pMouseY, pPartialTick);
        }

        // render hover outline
        GuiAreaDefinition lightDef = Arrays.stream(trafficLightLightAreas).filter(x -> x.isInBounds(pMouseX, pMouseY)).findFirst().orElse(null);
        if (lightDef != null) {
            GuiUtils.drawBox(graphics, lightDef, 0x55FFFFFF, 0xFFFFFFFF);
        } else if (trafficLightArea.isInBounds(pMouseX, pMouseY)) {
            GuiUtils.drawBox(graphics, trafficLightArea, 0x55FFFFFF, 0xFFFFFFFF);
        }

        // render controlling window
        DynamicGuiRenderer.renderWindow(graphics, ctrlWindowArea.getLeft(), ctrlWindowArea.getTop(), ctrlWindowArea.getWidth(), ctrlWindowArea.getHeight());
        DynamicGuiRenderer.renderArea(graphics, ctrlButtonsArea.getLeft(), ctrlButtonsArea.getTop(), ctrlButtonsArea.getWidth(), ctrlButtonsArea.getHeight(), AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderContainerBackground(graphics, ctrlSettingsArea.getLeft(), ctrlSettingsArea.getTop(), ctrlSettingsArea.getWidth(), ctrlSettingsArea.getHeight());
        GuiUtils.drawString(graphics, font, ctrlWindowArea.getLeft() + INNER_PADDING, ctrlWindowArea.getTop() + 7, TextUtils.translate(TrafficLightControlType.STATIC.getEnumTranslationKey(TrafficCraft.MOD_ID)), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);

        // render controltype tab
        switch (controlType) {
            case STATIC:
                renderControlTypeStatic(graphics, pMouseX, pMouseY, pPartialTick);
                break;
            case REMOTE:
                renderControlTypeRemote(graphics, pMouseX, pMouseY, pPartialTick);
                break;
            default:
                break;
        }
        super.renderMainLayer(graphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderFrontLayer(graphics, pMouseX, pMouseY, pPartialTick);

        if (Arrays.stream(trafficLightLightAreas).filter(x -> x.isInBounds(pMouseX, pMouseY)).findFirst().orElse(null) == null && trafficLightArea.isInBounds(pMouseX, pMouseY)) {
            trafficLightAreaTooltip.render(this, graphics, pMouseX, pMouseY);
        }
    }

    public void renderEmptyWindow(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        emptyLabel.renderCentered(graphics.poseStack(), guiLeft + WINDOW_PADDING_LEFT + (WINDOW_WIDTH - WINDOW_PADDING_LEFT) / 2, guiTop + 20 + 96 / 2 - emptyLabel.getLineCount() * 5, 10, 0xDBDBDB);
    }   

    public void renderGlobalWindow(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        DynamicGuiRenderer.renderWindow(graphics, guiLeft + WINDOW_PADDING_LEFT, guiTop + 20, WINDOW_WIDTH - WINDOW_PADDING_LEFT, 96);
        GuiUtils.drawTexture(WIDGETS_LOCATION, graphics, guiLeft + WINDOW_PADDING_LEFT - 9, guiTop + 20 + 96 / 2 - 9, 12, 18, 0, 0, 12, 18, 32, 32);

        DynamicGuiRenderer.renderArea(graphics, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING, WINDOW_WIDTH - WINDOW_PADDING_LEFT - INNER_PADDING * 2, 20, AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING + 25, TrafficLightModel.values().length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.DOWN);
        DynamicGuiRenderer.renderArea(graphics, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 20 + INNER_TOP_PADDING + 50, TrafficLightIcon.getAllowedForType(type).length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.DOWN);
        
        GuiUtils.drawString(graphics, font, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, guiTop + 27, textGeneralSettings, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }   
    
    public void renderPartEditor(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int windowHeight = INNER_TOP_PADDING + 27;
        int yBase = guiTop + 20;
        int y = 9 + (6 + TRAFFIC_LIGHT_LIGHT_SIZE) * selectedPart + yBase + TRAFFIC_LIGHT_LIGHT_SIZE / 2 - windowHeight / 2;

        DynamicGuiRenderer.renderWindow(graphics, guiLeft + WINDOW_PADDING_LEFT, y, TrafficLightColor.getAllowedForType(type, true).length * 18 + 2 + INNER_PADDING * 2, windowHeight);
        GuiUtils.drawTexture(WIDGETS_LOCATION, graphics, guiLeft + WINDOW_PADDING_LEFT - 9, y + windowHeight / 2 - 9, 12, 18, 0, 0, 12, 18, 32, 32);

        colorGroup.performForEach(x -> {
            x.y = y + INNER_TOP_PADDING + 1;
        });
        DynamicGuiRenderer.renderArea(graphics, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, y + INNER_TOP_PADDING, TrafficLightColor.getAllowedForType(type, true).length * 18 + 2, 20, AreaStyle.GRAY, ButtonState.DOWN);

        GuiUtils.drawString(graphics, font, guiLeft + WINDOW_PADDING_LEFT + INNER_PADDING, y + 7, textSetSignal, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    } 

    public void renderControlTypeStatic(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiUtils.drawString(graphics, font, ctrlSettingsArea.getLeft() + 4, ctrlSettingsArea.getTop() + DLIconButton.DEFAULT_BUTTON_HEIGHT / 2 - font.lineHeight / 2, textSetEnabledColors, 0xFFDBDBDB, EAlignment.LEFT, false);
    }

    public void renderControlTypeRemote(Graphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        GuiUtils.drawString(graphics, font, ctrlSettingsArea.getLeft() + 4, ctrlSettingsArea.getTop() + DLIconButton.DEFAULT_BUTTON_HEIGHT / 2 - font.lineHeight / 2, textSetPhaseId, 0xFFDBDBDB, EAlignment.LEFT, false);
        
        float scale = 0.75f;
        graphics.poseStack().pushPose();
        graphics.poseStack().scale(scale, scale, scale);
        phaseIdDescriptionLabel.renderLeftAlignedNoShadow(graphics.poseStack(), (int)((ctrlSettingsArea.getLeft() + 4) / SMALL_SCALE_VALUE), (int)((ctrlSettingsArea.getTop() + DLIconButton.DEFAULT_BUTTON_HEIGHT + 10) / SMALL_SCALE_VALUE), 10, 0xFFCBCBCB);
        graphics.poseStack().popPose();
    }
}
