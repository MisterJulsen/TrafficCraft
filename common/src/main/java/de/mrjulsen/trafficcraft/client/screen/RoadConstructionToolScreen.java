package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.VanillaSimpleButtonRenderer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.data.WorldLocation;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool.RoadBuilderCountResult;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderBuildRoadPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderDataPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderResetPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RoadConstructionToolScreen extends DLWindow {
    public static final Component title = TextUtils.translate("gui.trafficcraft.road_builder.title");

    private static final DLTexture GUI = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/road_construction_tool.png"), 256, 256);
    private static final int GUI_WIDTH = 244;
    private static final int GUI_HEIGHT = 179;

    private static final int WORKING_AREA_X = 7;
    private static final int WORKING_AREA_Y = 17;
    private static final int WORKING_AREA_WIDTH = 230;
    private static final int WORKING_AREA_HEIGHT = 155;
    private static final int WORKING_AREA_BOTTOM = WORKING_AREA_Y + WORKING_AREA_HEIGHT;
    @SuppressWarnings("unused")
    private static final int WORKING_AREA_RIGHT = WORKING_AREA_X + WORKING_AREA_WIDTH;
    

    // Controls
    private DLSlider widthSlider;
    private DLButton buildButton;

    private Rectangle pos1Area;
    private Rectangle pos2Area;
    private Rectangle buildButtonArea;
 

    // Settings
    private byte roadWidth;
    private boolean replaceExistingBlocks;
    private RoadType roadType = RoadType.ASPHALT;

    private final ItemStack stack;
    private final WorldLocation pos1;
    private final WorldLocation pos2;
    private int blocksCount;
    private int slopesCount;


    private final Component resetText = TextUtils.translate("gui.trafficcraft.road_builder.reset");
    private final Component buildText = TextUtils.translate("gui.trafficcraft.road_builder.build").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
    private final Component replaceBlocksText = TextUtils.translate("gui.trafficcraft.road_builder.replace_blocks");
    private final Component roadWidthText = TextUtils.translate("gui.trafficcraft.road_builder.road_width");
    private final Component roadBlocksText = TextUtils.translate("gui.trafficcraft.road_builder.road_blocks");
    private final Component requiredResourcesText = TextUtils.translate("gui.trafficcraft.road_builder.required_resources");
    private final Component noPositionDefined = TextUtils.translate("gui.trafficcraft.road_builder.no_pos_defined");

    private final Component tooltipPos1 = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.pos1");
    private final Component tooltipPos2 = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.pos2");
    private final Component tooltipReplaceBlocks = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.replace_blocks");
    private final Component tooltipReset = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.reset");
    private final Component tooltipBuild = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.build");
    private final Component tooltipBuildMissingPos = TextUtils.translate("gui.trafficcraft.road_builder.tooltip.build_missing_pos");


    public RoadConstructionToolScreen(DLWindowManager manager, ItemStack stack, int blocksCount, int slopesCount) {
        super(manager);
        setSize(GUI_WIDTH, GUI_HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

        if (!(stack.getItem() instanceof RoadConstructionTool)) {
            throw new IllegalArgumentException(stack.getDisplayName().getString() + " is not a valid item for screen 'RoadBuilderToolScreen'.");
        }

        CompoundTag nbt = stack.getOrCreateTag();
        pos1 = nbt.contains(RoadConstructionTool.NBT_LOCATION1) ? WorldLocation.loadFromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION1)) : null;
        pos2 = nbt.contains(RoadConstructionTool.NBT_LOCATION2) ? WorldLocation.loadFromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION2)) : null;
        roadWidth = nbt.getByte(RoadConstructionTool.NBT_ROAD_WIDTH);
        replaceExistingBlocks = nbt.getBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS);
        roadType = RoadType.getRoadTypeByIndex(nbt.getInt(RoadConstructionTool.NBT_ROAD_TYPE));

        this.stack = stack;
        this.blocksCount = blocksCount;
        this.slopesCount = slopesCount;
        
        pos1Area = Rectangle.withSize(7, 17, 114, 18);
        pos2Area = Rectangle.withSize(123, 17, 114, 18);


        /* Default page */

        int btnSpace = WORKING_AREA_WIDTH / 3;
        int btnWidth = btnSpace - 2;

        DLButton closeBtn = addComponent(new DLButton(WORKING_AREA_X, WORKING_AREA_BOTTOM - 20, btnWidth, 20));
        closeBtn.text.set(resetText);
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            ModNetworkManager.RESET_ROAD_BUILDER.send(NetworkDirection.toServer(), new RoadBuilderResetPacket());
            getWindowManager().closeWindow(this);
            return false;
        });
        closeBtn.tooltip.set(new DLTooltip(List.of(tooltipReset), 200));

        buildButton = addComponent(new DLButton(WORKING_AREA_X + btnSpace + 2, WORKING_AREA_BOTTOM - 20, btnWidth, 20));
        buildButton.text.set(buildText);
        buildButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            updateStackData();
            CompoundTag tag = this.stack.getOrCreateTag();
            WorldLocation pos1 = WorldLocation.loadFromNbt(tag.getCompound(RoadConstructionTool.NBT_LOCATION1));
            WorldLocation pos2 = WorldLocation.loadFromNbt(tag.getCompound(RoadConstructionTool.NBT_LOCATION2));
            byte roadWidth = tag.getByte(RoadConstructionTool.NBT_ROAD_WIDTH);
            boolean replaceBlocks = tag.getBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS);
            RoadType roadType = RoadType.getRoadTypeByIndex(tag.getInt(RoadConstructionTool.NBT_ROAD_TYPE));

            ModNetworkManager.ROAD_BUILDER_BUILD_ROAD.send(NetworkDirection.toServer(), new RoadBuilderBuildRoadPacket(pos1, pos2, roadWidth, replaceBlocks, roadType));
            RoadConstructionTool.reset(stack);
            ModNetworkManager.RESET_ROAD_BUILDER.send(NetworkDirection.toServer(), new RoadBuilderResetPacket());

            onDone();
            return false;
        });
        buildButton.tooltip.set(new DLTooltip(List.of(tooltipReset), 200));
        buildButton.enabled.set(pos1 != null && pos2 != null && roadWidth > 0);
        
        buildButtonArea = Rectangle.withSize(buildButton.x(), buildButton.y(), buildButton.width(), buildButton.height());

        DLButton doneBtn = addComponent(new DLButton(WORKING_AREA_X + (btnSpace * 2) + 4, WORKING_AREA_BOTTOM - 20, btnWidth, 20));
        doneBtn.text.set(CommonComponents.GUI_DONE);
        doneBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onDone();
            return false;
        });

        DLCycleButton<Boolean> replaceBtn = addComponent(new DLCycleButton<>(WORKING_AREA_X, 38, 114, 20));
        replaceBtn.text.set(replaceBlocksText);
        replaceBtn.selectedItem.set(Optional.of(replaceExistingBlocks));
        replaceBtn.tooltip.set(new DLTooltip(List.of(tooltipReplaceBlocks), 200));
        replaceBtn.cycling.set(true);
        replaceBtn.items.addAll(true, false);
        replaceBtn.textFormat.set((c) -> TextUtils.text(c.text.get().getString()).append(": ").append(c.selectedItem.get().map(b -> b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).orElse(CommonComponents.OPTION_OFF)));
        replaceBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            this.replaceExistingBlocks = replaceBtn.selectedItem.get().orElse(false);
            if (pos1 != null && pos2 != null) {
                RoadBuilderCountResult res = RoadConstructionTool.countBlocksNeeded(Minecraft.getInstance().level, pos1.getLocationVec3(), pos2.getLocationVec3(), roadWidth, replaceExistingBlocks);
                this.blocksCount = res.blocksCount;
                this.slopesCount = res.slopesCount;
            }
            return false;
        });

        this.widthSlider = addComponent(new DLSlider(WORKING_AREA_X + 116, 38, 114, 20));
        widthSlider.text.set(roadWidthText);
        widthSlider.min.set(1D);
        widthSlider.max.set((double)ModCommonConfig.ROAD_BUILDER_MAX_ROAD_WIDTH.get());
        widthSlider.value.set((double)roadWidth);
        widthSlider.addEventListener(DLSlider.ValueChangedEvent.class, (s, e) -> {
            roadWidth = (byte)e.value();
            if (pos1 != null && pos2 != null) {
                RoadBuilderCountResult res = RoadConstructionTool.countBlocksNeeded(Minecraft.getInstance().level, pos1.getLocationVec3(), pos2.getLocationVec3(), roadWidth, replaceExistingBlocks);
                this.blocksCount = res.blocksCount;
                this.slopesCount = res.slopesCount;
            }
            return false;
        });
        
        int blocksWidth = WORKING_AREA_WIDTH - 2;
        int buttonWidth = blocksWidth / (RoadType.values().length - 1);

        for (int i = 1; i < RoadType.values().length; i++) {
            RoadType type = RoadType.values()[i];

            ItemStack itemStack = new ItemStack(type.getBlock().asItem());
            DLToggleButton btn = addComponent(new DLToggleButton(WORKING_AREA_X + 1 + (buttonWidth * (i - 1)), 84, buttonWidth, 18));
            btn.componentRenderer.set(VanillaSimpleButtonRenderer.VANILLA_BUTTON_BROWN);
            btn.text.set(itemStack.getHoverName());
            btn.tooltip.set(new DLTooltip(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack), 200));
            btn.icon.set(new DLSprite(itemStack, 16, false));
            btn.radioButtonMode.set(true);
            btn.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
            btn.drawFontShadow.set(false);
            btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                this.roadType = type;
                return false;
            });

            if (type == roadType) {
                btn.checked.set(true);
            }
        }
    }

    private void updateStackData() {
        roadWidth = this.widthSlider.value.get().byteValue();
        CompoundTag nbt = this.stack.getOrCreateTag();
        nbt.putByte(RoadConstructionTool.NBT_ROAD_WIDTH, roadWidth);
        nbt.putBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS, replaceExistingBlocks);
        nbt.putInt(RoadConstructionTool.NBT_ROAD_TYPE, roadType.getIndex());
        ModNetworkManager.UPDATE_ROAD_BUILDER.send(NetworkDirection.toServer(), new RoadBuilderDataPacket(replaceExistingBlocks, roadWidth, roadType));
    }

    protected void onDone() {
        updateStackData();
        getWindowManager().closeWindow(this);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {        
        GuiUtils.drawTexture(GUI, graphics, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.CENTER, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), WORKING_AREA_X, 73, roadBlocksText, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), WORKING_AREA_X + 3, 107, requiredResourcesText, DLColor.fromInt(0xFFDBDBDB), ETextAlignment.LEFT, false);

        // render positions
        String pos1Text = pos1 == null ? noPositionDefined.getString() : String.format("%s, %s, %s", MathUtils.round(pos1.x, 2), MathUtils.round(pos1.y, 2), MathUtils.round(pos1.z, 2));
        String pos2Text = pos2 == null ? noPositionDefined.getString() : String.format("%s, %s, %s", MathUtils.round(pos2.x, 2), MathUtils.round(pos2.y, 2), MathUtils.round(pos2.z, 2));        
        GuiUtils.drawString(graphics, graphics.defaultFont(), WORKING_AREA_X + (114 / 2), 22, pos1Text, DLColor.fromInt(pos1 == null ? 0xFFDD2222 : 0xFF555555), ETextAlignment.CENTER, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), WORKING_AREA_X + 116 + (114 / 2), 22, pos2Text, DLColor.fromInt(pos2 == null ? 0xFFDD2222 : 0xFF555555), ETextAlignment.CENTER, false);

        // render required items
        if (pos1 != null && pos2 != null) {
            String blockCountText = String.format("x %s", blocksCount);
            String slopeCountText = String.format("x %s", slopesCount);
            int blockDisplayWidth = 20 + graphics.defaultFont().width(blockCountText);
            int slopeDisplayWidth = 20 + graphics.defaultFont().width(slopeCountText);
            int guiCenter = WORKING_AREA_X + WORKING_AREA_WIDTH / 2;

            graphics.graphics().renderItem(new ItemStack(roadType.getBlock()), guiCenter - WORKING_AREA_WIDTH / 4 - blockDisplayWidth / 2, 122);
            graphics.graphics().renderItem(new ItemStack(roadType.getSlope()), guiCenter + WORKING_AREA_WIDTH / 4 - slopeDisplayWidth / 2, 122);        
            GuiUtils.drawString(graphics, graphics.defaultFont(), guiCenter - WORKING_AREA_WIDTH / 4 - blockDisplayWidth / 2 + 20, 127, blockCountText, DLColor.fromInt(0xFFDBDBDB), ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), guiCenter + WORKING_AREA_WIDTH / 4 - slopeDisplayWidth / 2 + 20, 127, slopeCountText, DLColor.fromInt(0xFFDBDBDB), ETextAlignment.LEFT, false);
        }
    }

    @Override
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (buildButtonArea.collision(mouseX, mouseY)) {
            if (pos1 == null || pos2 == null) {
                GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, List.of(tooltipBuildMissingPos), 200);
            } else {
                GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, List.of(tooltipBuild), 200);
            }
        }
        if (pos1Area.collision(mouseX, mouseY)) {
            GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, List.of(tooltipPos1), 200);
        }
        if (pos2Area.collision(mouseX, mouseY)) {
            GuiUtils.drawTooltip(graphics, graphics.defaultFont(), (int)mouseX, (int)mouseY, List.of(tooltipPos2), 200);
        }
    }

}
