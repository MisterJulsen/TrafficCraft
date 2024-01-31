package de.mrjulsen.trafficcraft.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.WritableSignPacket;
import de.mrjulsen.trafficcraft.registry.ModBlocks;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class WritableSignScreen extends CommonScreen {
    /** Reference to the sign object. */
    protected final WritableTrafficSignBlockEntity sign;
    /** Counts the number of screen updates. */
    protected int frame;

    /** The index of the line that is being edited. */
    protected int selectedLine;
    protected TextFieldHelper signTextField;
    protected final WritableSignConfig config;
    protected final ConfiguredLine[] messages;
    protected final int lineCount;

    // Controls
    protected Button btnDone;

    public WritableSignScreen(WritableTrafficSignBlockEntity pSign) {
        super(Utils.translate("sign.edit"));

        this.config = new WritableSignConfig(ModBlocks.HOUSE_NUMBER_SIGN.get().defaultBlockState(), new ConfiguredLineData[] {
            new ConfiguredLineData(0, (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 0.5f)), 1, 3, (int)(WritableSignConfig.DEFAULT_SCALE * (1.0F / 16.0F * 8)), 0)
        }, 0, 120, WritableSignConfig.DEFAULT_SCALE, 0, 180, 0);//pSign.getRenderingConfig();

        this.lineCount = config.lineData.length;
        messages = IntStream.range(0, lineCount).mapToObj((i) -> {
            return new ConfiguredLine(pSign.getText(i), config.lineData[i]);
        }).toArray((length) -> {
            return new ConfiguredLine[length];
        });
        this.sign = pSign;
    }

    protected void init() {
        this.btnDone = addButton(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_169820_) -> {
            this.onDone();
        }, null);

        this.signTextField = new TextFieldHelper(() -> {
            return this.messages[this.selectedLine].text;
        }, (text) -> {
            this.messages[this.selectedLine].text = text;
            this.sign.setText(text, selectedLine);
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (text) -> {
            return text == null || this.minecraft.font.width(text) <= Arrays.stream(config.lineData).mapToInt(x -> x.maxWidth).max().getAsInt();
        });
    }

    public void removed() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new WritableSignPacket(this.sign.getBlockPos(), Arrays.stream(messages).map(x -> x.text).toArray(String[]::new))); 
    }

    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }

    }

    @Override
    protected void onDone() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new WritableSignPacket(this.sign.getBlockPos(), Arrays.stream(messages).map(x -> x.text).toArray(String[]::new))); 
        this.minecraft.setScreen(null);
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        this.signTextField.charTyped(pCodePoint);
        return true;
    }

    public void onClose() {
        this.onDone();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 265) {
            this.selectedLine = this.selectedLine - 1 & lineCount - 1;
            this.signTextField.setCursorToEnd();
            return true;
        } else if (pKeyCode != 264 && pKeyCode != 257 && pKeyCode != 335) {
            return this.signTextField.keyPressed(pKeyCode) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
        } else {
            this.selectedLine = this.selectedLine + 1 & lineCount - 1;
            this.signTextField.setCursorToEnd();
            return true;
        }
    }

    protected void renderSignBackground(GuiGraphics graphics) {
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        BlockState blockstate = config.state();
        PoseStack poseStack = graphics.pose();
        graphics.pose().translate((float)this.width / 2.0F - config.scale / 2 + config.xCenterOffset, config.y + config.scale / 2, 0);
        poseStack.scale(-config.scale, -config.scale, -1);
        poseStack.mulPose(Axis.XP.rotationDegrees(config.xRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(config.yRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(config.zRot()));

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(blockstate, graphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
    }

    public int getTextLineHeight() {
        return 10;
    }

    public Vector3f textScale(ConfiguredLine line) {
        float scale = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(this.font.width(line.text), line.data.maxWidth(), line.data.minScale(), line.data.maxScale());            
        return new Vector3f(scale, scale, 1);
    }

    private void renderSignText(GuiGraphics pGuiGraphics) {
        boolean flag = this.frame / 6 % 2 == 0;
        int cursorPos = this.signTextField.getCursorPos();
        int selectionPos = this.signTextField.getSelectionPos();
        int yCenter = this.messages.length * this.getTextLineHeight() / 2;
        int lineY = this.selectedLine * this.getTextLineHeight() - yCenter;

        for (int line = 0; line < this.messages.length; ++line) {
            pGuiGraphics.pose().pushPose();
            ConfiguredLine configuredLine = this.messages[line];
            
            Vector3f vector3f = this.textScale(configuredLine);
            pGuiGraphics.pose().translate((float)this.width / 2.0F + config.xCenterOffset + configuredLine.data.xOffset(), config.y + configuredLine.data.yOffset(), 5);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            if (configuredLine != null) {
                if (this.font.isBidirectional()) {
                    configuredLine.text = this.font.bidirectionalShaping(configuredLine.text);
                }

                int xCenter = -this.font.width(configuredLine.text) / 2;
                pGuiGraphics.drawString(this.font, configuredLine.text, xCenter, line * this.getTextLineHeight() - yCenter, configuredLine.data.color(), false);
                if (line == this.selectedLine && cursorPos >= 0 && flag) {
                    int l1 = this.font.width(configuredLine.text.substring(0, Math.max(Math.min(cursorPos, configuredLine.text.length()), 0)));
                    int i2 = l1 - this.font.width(configuredLine.text) / 2;
                    if (cursorPos >= configuredLine.text.length()) {
                        pGuiGraphics.drawString(this.font, "_", i2, lineY, configuredLine.data.color(), false);
                    }
                }
            }
            pGuiGraphics.pose().popPose();
        }

        for (int lineHighlight = 0; lineHighlight < this.messages.length; ++lineHighlight) {
            ConfiguredLine configuredLineH = this.messages[lineHighlight];
            pGuiGraphics.pose().pushPose();
            Vector3f vector3f = this.textScale(configuredLineH);
            pGuiGraphics.pose().translate((float)this.width / 2.0F + config.xCenterOffset + configuredLineH.data.xOffset(), config.y + configuredLineH.data.yOffset, 5);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            if (configuredLineH != null && lineHighlight == this.selectedLine && cursorPos >= 0) {
                int l3 = this.font.width(configuredLineH.text.substring(0, Math.max(Math.min(cursorPos, configuredLineH.text.length()), 0)));
                int i4 = l3 - this.font.width(configuredLineH.text) / 2;
                if (flag && cursorPos < configuredLineH.text.length()) {
                    pGuiGraphics.fill(i4, lineY - 1, i4 + 1, lineY + this.getTextLineHeight(), -16777216 | configuredLineH.data.color());
                }

                if (selectionPos != cursorPos) {
                    int j4 = Math.min(cursorPos, selectionPos);
                    int j2 = Math.max(cursorPos, selectionPos);
                    int k2 = this.font.width(configuredLineH.text.substring(0, j4)) - this.font.width(configuredLineH.text) / 2;
                    int l2 = this.font.width(configuredLineH.text.substring(0, j2)) - this.font.width(configuredLineH.text) / 2;
                    int i3 = Math.min(k2, l2);
                    int j3 = Math.max(k2, l2);
                    pGuiGraphics.fill(RenderType.guiTextHighlight(), i3, lineY, j3, lineY + this.getTextLineHeight(), -16776961);
                }
            }
            pGuiGraphics.pose().popPose();
        }

    }
    public void renderSign(GuiGraphics graphics) {
        graphics.pose().setIdentity();
        graphics.pose().pushPose();
        graphics.pose().pushPose();
        this.renderSignBackground(graphics);
        graphics.pose().popPose();
        this.renderSignText(graphics);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Lighting.setupForFlatItems();
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
        this.renderSign(graphics);
        Lighting.setupFor3DItems();
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
    }

    public static record WritableSignConfig(BlockState state, ConfiguredLineData[] lineData, int xCenterOffset, int y, int scale, int xRot, int yRot, int zRot) {
        public static final int DEFAULT_SCALE = 96;
    }

    public static record ConfiguredLineData(int xOffset, int yOffset, float minScale, float maxScale, int maxWidth, int color) {}
    protected static class ConfiguredLine {
        public String text;
        public final ConfiguredLineData data;

        public ConfiguredLine(String text, ConfiguredLineData data) {
            this.text = text;
            this.data = data;
        }
    }
}
