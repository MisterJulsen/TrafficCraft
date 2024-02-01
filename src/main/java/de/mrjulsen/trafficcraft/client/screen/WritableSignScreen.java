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

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

    public static final int DEFAULT_LINE_HEIGHT = 10;

    protected final WritableTrafficSignBlockEntity sign;
    protected final BlockState blockState;
    protected final WritableSignConfig config;
    protected final ConfiguredLine[] messages;
    protected final int lineCount;

    protected int blinkFrame;
    protected int selectedLine;
    protected TextFieldHelper signTextField;

    // Controls
    protected Button btnDone;

    public WritableSignScreen(WritableTrafficSignBlockEntity pSign) {
        this(pSign, pSign.getRenderConfig(), pSign.getBlockState().getBlock().defaultBlockState(), getMessages(pSign, pSign.getRenderConfig()));
    }

    protected WritableSignScreen(WritableTrafficSignBlockEntity pSign, WritableSignConfig config, BlockState state, ConfiguredLine[] messages) {
        super(Utils.translate("sign.edit"));

        this.config = config;
        this.blockState = state;
        this.sign = pSign;
        
        this.messages = messages;
        this.lineCount = this.messages.length;

    }

    protected static ConfiguredLine[] getMessages(WritableTrafficSignBlockEntity pSign, WritableSignConfig config) {
        return IntStream.range(0, config.lineData.length).mapToObj((i) -> {
            return new ConfiguredLine(pSign.getText(i), config.lineData[i]);
        }).toArray((length) -> {
            return new ConfiguredLine[length];
        });
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
            return text == null || this.minecraft.font.width(text) <= config.lineData[this.selectedLine].maxLineWidth() * config.scale();
        });
    }

    public void removed() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new WritableSignPacket(this.sign.getBlockPos(), Arrays.stream(messages).map(x -> x.text).toArray(String[]::new))); 
    }

    public void tick() {
        ++this.blinkFrame;
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
        PoseStack poseStack = graphics.pose();
        graphics.pose().translate((float)this.width / 2.0F + config.scale / 2 + config.xCenterOffset * config.scale, config.y + config.scale / 2, 100);
        poseStack.scale(-config.scale, -config.scale, -1);
        poseStack.mulPose(Axis.YP.rotationDegrees(config.yRot()));

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(blockState, graphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
    }

    private void renderSignText(GuiGraphics pGuiGraphics) {
        boolean flag = this.blinkFrame / 6 % 2 == 0;
        int cursorPos = this.signTextField.getCursorPos();
        int selectionPos = this.signTextField.getSelectionPos();

        for (int line = 0; line < this.messages.length; ++line) {
            pGuiGraphics.pose().pushPose();
            ConfiguredLine configuredLine = this.messages[line];
            
            Vector3f vector3f = config.screenTextScale(font, config.scale(), configuredLine);
            pGuiGraphics.pose().translate((float)this.width / 2.0F + configuredLine.data.xOffset() * config.scale(), config.y + configuredLine.data.yOffset() * config.scale - (int)(WritableSignScreen.DEFAULT_LINE_HEIGHT / 2 * config.lineData()[0].lineHeightScale()) + config.getLineHeightsUntil(line) + config.getLineOffset(line, vector3f.y), 105);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            if (configuredLine != null) {
                if (this.font.isBidirectional()) {
                    configuredLine.text = this.font.bidirectionalShaping(configuredLine.text);
                }

                int xCenter = -this.font.width(configuredLine.text) / 2;
                pGuiGraphics.drawString(this.font, configuredLine.text, xCenter, 0, configuredLine.data.color(), false);
                if (line == this.selectedLine && cursorPos >= 0 && flag) {
                    int l1 = this.font.width(configuredLine.text.substring(0, Math.max(Math.min(cursorPos, configuredLine.text.length()), 0)));
                    int i2 = l1 - this.font.width(configuredLine.text) / 2;
                    if (cursorPos >= configuredLine.text.length()) {
                        pGuiGraphics.drawString(this.font, "_", i2, 0, configuredLine.data.color(), false);
                    }
                }
            }
            pGuiGraphics.pose().popPose();
        }

        for (int lineHighlight = 0; lineHighlight < this.messages.length; ++lineHighlight) {
            ConfiguredLine configuredLineH = this.messages[lineHighlight];
            pGuiGraphics.pose().pushPose();
            Vector3f vector3f = config.screenTextScale(font, config.scale(), configuredLineH);
            pGuiGraphics.pose().translate((float)this.width / 2.0F + configuredLineH.data.xOffset() * config.scale(), config.y + configuredLineH.data.yOffset() * config.scale - (int)(WritableSignScreen.DEFAULT_LINE_HEIGHT / 2 * config.lineData()[0].lineHeightScale()) + config.getLineHeightsUntil(lineHighlight + 1) - config.halfLineHeight(lineHighlight) + config.getHalfLineHeightScales(lineHighlight, vector3f.y), 105);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            float lineScale = config.lineData()[lineHighlight].lineHeightScale();

            if (configuredLineH != null && lineHighlight == this.selectedLine && cursorPos >= 0) {
                int l3 = this.font.width(configuredLineH.text.substring(0, Math.max(Math.min(cursorPos, configuredLineH.text.length()), 0)));
                int i4 = l3 - this.font.width(configuredLineH.text) / 2;
                if (flag && cursorPos < configuredLineH.text.length()) {
                    pGuiGraphics.fill(i4, -(int)(DEFAULT_LINE_HEIGHT * (lineScale + 1) * 0.5F), i4 + 1, -(int)(DEFAULT_LINE_HEIGHT * (lineScale - 1) * 0.5F), -16777216 | configuredLineH.data.color());
                }

                if (selectionPos != cursorPos) {
                    int j4 = Math.min(cursorPos, selectionPos);
                    int j2 = Math.max(cursorPos, selectionPos);
                    int k2 = this.font.width(configuredLineH.text.substring(0, j4)) - this.font.width(configuredLineH.text) / 2;
                    int l2 = this.font.width(configuredLineH.text.substring(0, j2)) - this.font.width(configuredLineH.text) / 2;
                    int i3 = Math.min(k2, l2);
                    int j3 = Math.max(k2, l2);
                    pGuiGraphics.fill(RenderType.guiTextHighlight(), i3, -(int)(DEFAULT_LINE_HEIGHT * (lineScale + 1) * 0.5F), j3, -(int)(DEFAULT_LINE_HEIGHT * (lineScale - 1) * 0.5F), -16776961);
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

    public static record WritableSignConfig(ConfiguredLineData[] lineData, boolean renderBack, float xCenterOffset, float y, float scale, float yRot, float berX, float berY, float berZ, Function<BlockState, Float> blockEntityRendererRotation, int berColor) {
        public static final int DEFAULT_SCALE = 96;

        public int getLineHeightsUntil(int index) {
            return (int)IntStream.range(0, index).mapToLong(x -> (int)(lineData()[x].lineHeightScale() * WritableSignScreen.DEFAULT_LINE_HEIGHT)).sum();
        }
    
        public int getLineOffset(int index, float currentScaleY) {
            float halfLineHeight = halfLineHeight(index);
            return (int)(halfLineHeight - (WritableSignScreen.DEFAULT_LINE_HEIGHT * 0.5F * currentScaleY));
        }
        
        public float lineHeight(int index) {
            return lineData()[index].lineHeightScale() * WritableSignScreen.DEFAULT_LINE_HEIGHT;
        }
    
        public float halfLineHeight(int index) {
            return lineData()[index].lineHeightScale() * WritableSignScreen.DEFAULT_LINE_HEIGHT * 0.5F;
        }
    
        public float getHalfLineHeightScales(int index, float currentScaleY) {
            return halfLineHeight(index) * currentScaleY;
        }
    
        public Vector3f screenTextScale(Font font, float scale, ConfiguredLine line) {
            float scaleX = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(font.width(line.text), line.data.maxLineWidth() * scale, line.data.minScale().x, line.data.maxScale().x);            
            float scaleY = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(font.width(line.text), line.data.maxLineWidth() * scale, line.data.minScale().y, line.data.maxScale().y);            
            return new Vector3f(scaleX, scaleY, 1);
        }

        public Vector3f berTextScale(String text, Font font, float scale, ConfiguredLineData data) {
            float scaleX = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(font.width(text) * scale, data.maxLineWidth(), data.minScale().x, data.maxScale().x);            
            float scaleY = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(font.width(text) * scale, data.maxLineWidth(), data.minScale().y, data.maxScale().y);            
            return new Vector3f(scaleX, scaleY, 1);
        }
    }

    public static record ConfiguredLineData(float xOffset, float yOffset, Vector2f minScale, Vector2f maxScale, float maxLineWidth, float lineHeightScale, int color) {}
    protected static class ConfiguredLine {
        public String text;
        public final ConfiguredLineData data;

        public ConfiguredLine(String text, ConfiguredLineData data) {
            this.text = text;
            this.data = data;
        }
    }
}
