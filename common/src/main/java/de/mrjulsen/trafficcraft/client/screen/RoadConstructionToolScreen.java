package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.core.Location;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.config.ModCommonConfig;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool.RoadBuilderCountResult;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderBuildRoadPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderDataPacket;
import de.mrjulsen.trafficcraft.network.packets.cts.RoadBuilderResetPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RoadConstructionToolScreen extends DLScreen {
    public static final Component title = TextUtils.translate("gui.trafficcraft.road_builder.title");

    private static final ResourceLocation GUI = new ResourceLocation(TrafficCraft.MOD_ID, "textures/gui/road_construction_tool.png");
    private static final int GUI_WIDTH = 244;
    private static final int GUI_HEIGHT = 179;
    private static final int WORKING_AREA_X = 7;
    private static final int WORKING_AREA_Y = 17;
    private static final int WORKING_AREA_WIDTH = 230;
    private static final int WORKING_AREA_HEIGHT = 155;
    private static final int WORKING_AREA_BOTTOM = WORKING_AREA_Y + WORKING_AREA_HEIGHT;
    @SuppressWarnings("unused")
    private static final int WORKING_AREA_RIGHT = WORKING_AREA_X + WORKING_AREA_WIDTH;
    

    private int guiTop, guiLeft;    

    // Controls
    private final WidgetsCollection itemButtonCollection = new WidgetsCollection();
    private DLSlider widthSlider;
    private DLButton buildButton;
    private GuiAreaDefinition pos1Area;
    private GuiAreaDefinition pos2Area;
    private GuiAreaDefinition buildButtonArea;    

    // Settings
    private byte roadWidth;
    private boolean replaceExistingBlocks;
    private RoadType roadType = RoadType.ASPHALT;

    private final ItemStack stack;
    private final Location pos1;
    private final Location pos2;
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


    public RoadConstructionToolScreen(ItemStack stack, int blocksCount, int slopesCount) {
        super(title);

        if (!(stack.getItem() instanceof RoadConstructionTool)) {
            throw new IllegalArgumentException(stack.getDisplayName().getString() + " is not a valid item for screen 'RoadBuilderToolScreen'.");
        }

        CompoundTag nbt = stack.getOrCreateTag();
        pos1 = Location.fromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION1));
        pos2 = Location.fromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION2));
        roadWidth = nbt.getByte(RoadConstructionTool.NBT_ROAD_WIDTH);
        replaceExistingBlocks = nbt.getBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS);
        roadType = RoadType.getRoadTypeByIndex(nbt.getInt(RoadConstructionTool.NBT_ROAD_TYPE));

        this.stack = stack;
        this.blocksCount = blocksCount;
        this.slopesCount = slopesCount;
    }

    @Override
    public void init() {
        super.init();
        
        guiLeft = this.width / 2 - GUI_WIDTH / 2;
        guiTop = this.height / 2 - GUI_HEIGHT / 2;
        itemButtonCollection.components.clear();

        pos1Area = new GuiAreaDefinition(guiLeft + 7, guiTop + 17, 114, 18);
        pos2Area = new GuiAreaDefinition(guiLeft + 123, guiTop + 17, 114, 18);


        /* Default page */

        int btnSpace = WORKING_AREA_WIDTH / 3;
        int btnWidth = btnSpace - 2;

        addButton(guiLeft + WORKING_AREA_X + (btnSpace * 0), guiTop + WORKING_AREA_BOTTOM - 20, btnWidth, 20, resetText, (p) -> {
            TrafficCraft.net().sendToServer(new RoadBuilderResetPacket());
            this.onClose();
        }, DLTooltip.of(tooltipReset).withMaxWidth(width / 4));

        this.buildButton = addButton(guiLeft + WORKING_AREA_X + (btnSpace * 1) + 2, guiTop + WORKING_AREA_BOTTOM - 20, btnWidth, 20, buildText, (p) -> {
            updateStackData();
            CompoundTag nbt = this.stack.getOrCreateTag();
            Location pos1 = Location.fromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION1));
            Location pos2 = Location.fromNbt(nbt.getCompound(RoadConstructionTool.NBT_LOCATION2));
            byte roadWidth = nbt.getByte(RoadConstructionTool.NBT_ROAD_WIDTH);
            boolean replaceBlocks = nbt.getBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS);
            RoadType roadType = RoadType.getRoadTypeByIndex(nbt.getInt(RoadConstructionTool.NBT_ROAD_TYPE));

            TrafficCraft.net().sendToServer(new RoadBuilderBuildRoadPacket(pos1, pos2, roadWidth, replaceBlocks, roadType));

            RoadConstructionTool.reset(stack);
            TrafficCraft.net().sendToServer(new RoadBuilderResetPacket());
            this.onDone();
        }, null);
        buildButton.active = pos1 != null && pos2 != null && roadWidth > 0;
        buildButtonArea = new GuiAreaDefinition(buildButton.x, buildButton.y, buildButton.getWidth(), buildButton.getHeight());

        addButton(guiLeft + WORKING_AREA_X + (btnSpace * 2) + 4, guiTop + WORKING_AREA_BOTTOM - 20, btnWidth, 20, CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }, null);

        addOnOffButton(guiLeft + WORKING_AREA_X, guiTop + 38, 114, 20, replaceBlocksText, this.replaceExistingBlocks, (btn, value) -> {
            this.replaceExistingBlocks = value;

            if (pos1 != null && pos2 != null) {
                RoadBuilderCountResult res = RoadConstructionTool.countBlocksNeeded(minecraft.level, pos1.getLocationVec3(), pos2.getLocationVec3(), roadWidth, replaceExistingBlocks);
                blocksCount = res.blocksCount;
                slopesCount = res.slopesCount;
            }
        }, DLTooltip.of(tooltipReplaceBlocks).withMaxWidth(width / 4));

        this.widthSlider = addSlider(guiLeft + WORKING_AREA_X + 116, guiTop + 38, 114, 20, roadWidthText, TextUtils.text(""), 1, ModCommonConfig.ROAD_BUILDER_MAX_ROAD_WIDTH.get(), 1, this.roadWidth, true,
        (slider, value) -> {
            roadWidth = value.byteValue();
            if (pos1 != null && pos2 != null) {
                RoadBuilderCountResult res = RoadConstructionTool.countBlocksNeeded(minecraft.level, pos1.getLocationVec3(), pos2.getLocationVec3(), roadWidth, replaceExistingBlocks);
                blocksCount = res.blocksCount;
                slopesCount = res.slopesCount;
            }
        }, null, null);
        
        int blocksWidth = WORKING_AREA_WIDTH - 2;
        int buttonWidth = blocksWidth / (RoadType.values().length - 1);

        for (int i = 1; i < RoadType.values().length; i++) {
            RoadType type = RoadType.values()[i];

            DLItemButton btn = this.addRenderableWidget(new DLItemButton(
                ButtonType.RADIO_BUTTON,
                AreaStyle.BROWN,
                new ItemStack(type.getBlock().asItem()),
                itemButtonCollection,
                guiLeft + WORKING_AREA_X + 1 + (buttonWidth * (i - 1)),
                guiTop + 84,
                buttonWidth,
                DLItemButton.DEFAULT_BUTTON_HEIGHT,
                null,
                (p) -> {
                    this.roadType = type;
                }).withAlignment(EAlignment.LEFT)
            );

            if (type == roadType) {
                btn.select();
            }
        }

        addTooltip(DLTooltip.of(tooltipPos1).assignedTo(pos1Area));
        addTooltip(DLTooltip.of(tooltipPos2).assignedTo(pos2Area));
    }

    private void updateStackData() {
        roadWidth = (byte)this.widthSlider.getValue();
        CompoundTag nbt = this.stack.getOrCreateTag();
        nbt.putByte(RoadConstructionTool.NBT_ROAD_WIDTH, roadWidth);
        nbt.putBoolean(RoadConstructionTool.NBT_REPLACE_BLOCKS, replaceExistingBlocks);
        nbt.putInt(RoadConstructionTool.NBT_ROAD_TYPE, roadType.getIndex());
        TrafficCraft.net().sendToServer(new RoadBuilderDataPacket(replaceExistingBlocks, roadWidth, roadType));
    }

    @Override
    protected void onDone() {
        updateStackData();
        onClose();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {        
        renderScreenBackground(graphics);
        
        GuiUtils.drawTexture(GUI, graphics, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        GuiUtils.drawString(graphics, font, this.width / 2 - font.width(title) / 2, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + WORKING_AREA_X, guiTop + 73, roadBlocksText, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + WORKING_AREA_X + 3, guiTop + 107, requiredResourcesText, DragonLib.NATIVE_BUTTON_FONT_COLOR_HIGHLIGHT, EAlignment.LEFT, false);

        // render positions
        String pos1Text = pos1 == null ? noPositionDefined.getString() : String.format("%s, %s, %s", MathUtils.round(pos1.x, 2), MathUtils.round(pos1.y, 2), MathUtils.round(pos1.z, 2));
        String pos2Text = pos2 == null ? noPositionDefined.getString() : String.format("%s, %s, %s", MathUtils.round(pos2.x, 2), MathUtils.round(pos2.y, 2), MathUtils.round(pos2.z, 2));        
        GuiUtils.drawString(graphics, font, guiLeft + WORKING_AREA_X + (114 / 2), guiTop + 22, pos1Text, pos1 == null ? 0xFFDD2222 : 0xFF555555, EAlignment.CENTER, false);
        GuiUtils.drawString(graphics, font, guiLeft + WORKING_AREA_X + 116 + (114 / 2), guiTop + 22, pos2Text, pos2 == null ? 0xFFDD2222 : 0xFF555555, EAlignment.CENTER, false);

        // render required items
        if (pos1 != null && pos2 != null) {
            String blockCountText = String.format("x %s", blocksCount);
            String slopeCountText = String.format("x %s", slopesCount);
            int blockDisplayWidth = 20 + font.width(blockCountText);
            int slopeDisplayWidth = 20 + font.width(slopeCountText);
            int guiCenter = guiLeft + WORKING_AREA_X + WORKING_AREA_WIDTH / 2;

            minecraft.getItemRenderer().renderAndDecorateItem(new ItemStack(roadType.getBlock()), guiCenter - WORKING_AREA_WIDTH / 4 - blockDisplayWidth / 2, guiTop + 122);
            minecraft.getItemRenderer().renderAndDecorateItem(new ItemStack(roadType.getSlope()), guiCenter + WORKING_AREA_WIDTH / 4 - slopeDisplayWidth / 2, guiTop + 122);        
            GuiUtils.drawString(graphics, font, guiCenter - WORKING_AREA_WIDTH / 4 - blockDisplayWidth / 2 + 20, guiTop + 127, blockCountText, 0xFFDBDBDB, EAlignment.LEFT, false);
            GuiUtils.drawString(graphics, font, guiCenter + WORKING_AREA_WIDTH / 4 - slopeDisplayWidth / 2 + 20, guiTop + 127, slopeCountText, 0xFFDBDBDB, EAlignment.LEFT, false);
        }
        
        // default rendering
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);

        // Tooltips
        if (buildButtonArea.isInBounds(mouseX, mouseY)) {
            if (pos1 == null || pos2 == null) {
                GuiUtils.renderTooltip(this, buildButtonArea, List.of(tooltipBuildMissingPos), width / 4, graphics, mouseX, mouseY);
            } else {
                GuiUtils.renderTooltip(this, buildButton, List.of(tooltipBuild), width / 4, graphics, mouseX, mouseY);
            }
        }
    }

}
