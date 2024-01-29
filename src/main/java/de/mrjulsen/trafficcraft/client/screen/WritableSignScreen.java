package de.mrjulsen.trafficcraft.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig.IFontScale;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.WritableSignPacket;

import java.util.stream.IntStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.DyeColor;
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
    protected int line;
    protected TextFieldHelper signField;
    protected final String[] messages;
    protected final int lines;
    protected final SignRenderingConfig config;

    // Controls
    protected Button btnDone;

    public WritableSignScreen(WritableTrafficSignBlockEntity pSign) {
        super(Utils.translate("sign.edit"));

        this.config = pSign.getRenderingConfig();
        this.lines = pSign.getRenderingConfig().getLines();
        this.messages = IntStream.range(0, lines).mapToObj((i) -> {
            return pSign.getText(i);
        }).toArray((length) -> {
            return new String[length];
        });
        this.sign = pSign;
    }

    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.btnDone = addButton(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_169820_) -> {
            this.onDone();
        }, null);

        this.signField = new TextFieldHelper(() -> {
            return this.messages[this.line];
        }, (text) -> {
            this.messages[this.line] = text;
            this.sign.setText(text, line);
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (text) -> {
            return text == null || this.minecraft.font.width(text) <= config.maxLineWidth;
        });
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new WritableSignPacket(this.sign.getBlockPos(), messages)); 
    }

    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }

    }

    @Override
    protected void onDone() {
        NetworkManager.getInstance().sendToServer(ClientTools.getConnection(), new WritableSignPacket(this.sign.getBlockPos(), messages)); 
        this.minecraft.setScreen(null);
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        this.signField.charTyped(pCodePoint);
        return true;
    }

    public void onClose() {
        this.onDone();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 265) {
            this.line = this.line - 1 & lines - 1;
            this.signField.setCursorToEnd();
            return true;
        } else if (pKeyCode != 264 && pKeyCode != 257 && pKeyCode != 335) {
            return this.signField.keyPressed(pKeyCode) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
        } else {
            this.line = this.line + 1 & lines - 1;
            this.signField.setCursorToEnd();
            return true;
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Lighting.setupForFlatItems();
        this.renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 40, 16777215);

        BlockState blockstate = this.sign.getBlockState().getBlock().defaultBlockState();

        // Render sign
        pPoseStack.pushPose();
        pPoseStack.setIdentity();
        pPoseStack.translate((double)this.width / 2 + config.scale / 2 + config.textureXOffset, config.scale + config.textureYOffset, (double)-config.scale);
        pPoseStack.scale(config.scale, config.scale, -config.scale);
        pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(config.modelRotation));
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, pPoseStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
        pPoseStack.popPose();

        // Text rendering
        
        int i = DyeColor.BLACK.getTextColor();
        int j = this.signField.getCursorPos();
        int k = this.signField.getSelectionPos();
        String msg = this.messages[this.line];
        int l = config.getLineHeightsTo(font.lineHeight, this.line, msg == null ? 0 : this.font.width(this.messages[this.line]), config.maxLineWidth) - this.messages.length * 5;
        pPoseStack.setIdentity();
        pPoseStack.translate((double)this.width / 2, config.textYOffset, 10);
        Matrix4f matrix4f = pPoseStack.last().pose();

        // Cursor blinking        
        boolean flag1 = this.frame / 6 % 2 == 0;

        for (int i1 = 0; i1 < this.messages.length; ++i1) {
            String s = this.messages[i1];
            IFontScale scaleConfig = config.getFontScale(i1);
            float scale = scaleConfig == null ? 1.0F : (float)scaleConfig.getScale(this.font.width(s), config.maxLineWidth);            
            pPoseStack.setIdentity();
            pPoseStack.translate((double)this.width / 2, config.textYOffset, 10);
            pPoseStack.scale(scale, scale, -scale);
            matrix4f = pPoseStack.last().pose();

            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                float f3 = (float) (-this.minecraft.font.width(s) / 2);
                this.minecraft.font.drawInBatch(s, f3, (float) (config.getLineHeightsTo(font.lineHeight, i1, s == null ? 0 : this.font.width(s), config.maxLineWidth) - this.messages.length * 5), i, false, matrix4f,
                        multibuffersource$buffersource, false, 0, 15728880, false);
                if (i1 == this.line && j >= 0 && flag1) {
                    int j1 = this.minecraft.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
                    int k1 = j1 - this.minecraft.font.width(s) / 2;
                    if (j >= s.length()) {
                        this.minecraft.font.drawInBatch("_", (float) k1, (float) l, i, false, matrix4f,
                                multibuffersource$buffersource, false, 0, 15728880, false);
                    }
                }
            }
        }

        multibuffersource$buffersource.endBatch();

        for (int i3 = 0; i3 < this.messages.length; ++i3) {
            String s1 = this.messages[i3];
            IFontScale scaleConfig = config.getFontScale(i3);
            float scale = scaleConfig == null ? 1.0F : (float)scaleConfig.getScale(this.font.width(s1), config.maxLineWidth);            
            pPoseStack.setIdentity();
            pPoseStack.translate((double)this.width / 2, config.textYOffset, 10);
            pPoseStack.scale(scale, scale, -scale);
            matrix4f = pPoseStack.last().pose();

            if (s1 != null && i3 == this.line && j >= 0) {
                int j3 = this.minecraft.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int k3 = j3 - this.minecraft.font.width(s1) / 2;
                if (flag1 && j < s1.length()) {
                    fill(pPoseStack, k3, l - 1, k3 + 1, l + 9, -16777216 | i);
                }

                if (k != j) {
                    int l3 = Math.min(j, k);
                    int l1 = Math.max(j, k);
                    int i2 = this.minecraft.font.width(s1.substring(0, l3)) - this.minecraft.font.width(s1) / 2;
                    int j2 = this.minecraft.font.width(s1.substring(0, l1)) - this.minecraft.font.width(s1) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(matrix4f, (float) k2, (float) (l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) l2, (float) (l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) l2, (float) l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float) k2, (float) l, 0.0F).color(0, 0, 255, 255).endVertex();
                    BufferUploader.drawWithShader(bufferbuilder.end());
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        pPoseStack.popPose();
        Lighting.setupFor3DItems();
        
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
