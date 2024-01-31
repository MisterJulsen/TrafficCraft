package de.mrjulsen.trafficcraft.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import de.mrjulsen.mcdragonlib.client.gui.wrapper.CommonScreen;
import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.SignRenderingConfig;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.cts.WritableSignPacket;
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
    protected int selectedLine;
    protected TextFieldHelper signTextField;
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
        this.btnDone = addButton(this.width / 2 - 100, this.height / 4 + 120, 200, 20, CommonComponents.GUI_DONE, (p_169820_) -> {
            this.onDone();
        }, null);

        this.signTextField = new TextFieldHelper(() -> {
            return this.messages[this.selectedLine];
        }, (text) -> {
            this.messages[this.selectedLine] = text;
            this.sign.setText(text, selectedLine);
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (text) -> {
            return text == null || this.minecraft.font.width(text) <= config.maxLineWidth;
        });
    }

    public void removed() {
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
        this.signTextField.charTyped(pCodePoint);
        return true;
    }

    public void onClose() {
        this.onDone();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 265) {
            this.selectedLine = this.selectedLine - 1 & lines - 1;
            this.signTextField.setCursorToEnd();
            return true;
        } else if (pKeyCode != 264 && pKeyCode != 257 && pKeyCode != 335) {
            return this.signTextField.keyPressed(pKeyCode) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
        } else {
            this.selectedLine = this.selectedLine + 1 & lines - 1;
            this.signTextField.setCursorToEnd();
            return true;
        }
    }

    private final int scale = 96;
    private final int xOffset = 0;
    private final int yOffset = 120;
    private final float minScale = 1;
    private final float maxScale = 3;
    private final int maxWidth = (int)(scale * (1.0F / 16.0F * 8));
    private final int textXOffset = 0;
    private final int textYOffset = (int)(scale * (1.0F / 16.0F * 0.5f));

    protected void renderSignBackground(GuiGraphics graphics) {
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        BlockState blockstate = this.sign.getBlockState().getBlock().defaultBlockState();
        PoseStack poseStack = graphics.pose();
        graphics.pose().translate((float)this.width / 2.0F - scale / 2 + xOffset, yOffset + scale / 2, 0);
        poseStack.scale(-scale, -scale, -1);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(blockstate, graphics.pose(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
    }

    public int getTextLineHeight() {
        return 10;
    }

    public Vector3f textScale(String s) {
        float scale = (float)de.mrjulsen.trafficcraft.util.Utils.getScale(this.font.width(s), maxWidth, minScale, maxScale);            
        return new Vector3f(scale, scale, 1);
    }

    private void renderSignText(GuiGraphics pGuiGraphics) {
        //pGuiGraphics.pose().translate(0.0F, 0.0F, 4.0F);
        pGuiGraphics.pose().translate((float)this.width / 2.0F + xOffset + textXOffset, yOffset + textYOffset, 5);
        int color = DyeColor.BLACK.getTextColor();
        boolean flag = this.frame / 6 % 2 == 0;
        int cursorPos = this.signTextField.getCursorPos();
        int selectionPos = this.signTextField.getSelectionPos();
        int yCenter = this.messages.length * this.getTextLineHeight() / 2;
        int lineY = this.selectedLine * this.getTextLineHeight() - yCenter;

        for (int line = 0; line < this.messages.length; ++line) {
            pGuiGraphics.pose().pushPose();
            String s = this.messages[line];
            
            Vector3f vector3f = this.textScale(s);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                int xCenter = -this.font.width(s) / 2;
                pGuiGraphics.drawString(this.font, s, xCenter, line * this.getTextLineHeight() - yCenter, color, false);
                if (line == this.selectedLine && cursorPos >= 0 && flag) {
                    int l1 = this.font.width(s.substring(0, Math.max(Math.min(cursorPos, s.length()), 0)));
                    int i2 = l1 - this.font.width(s) / 2;
                    if (cursorPos >= s.length()) {
                        pGuiGraphics.drawString(this.font, "_", i2, lineY, color, false);
                    }
                }
            }
            pGuiGraphics.pose().popPose();
        }

        for (int lineHighlight = 0; lineHighlight < this.messages.length; ++lineHighlight) {
            String s1 = this.messages[lineHighlight];
            pGuiGraphics.pose().pushPose();            
            Vector3f vector3f = this.textScale(s1);
            pGuiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());

            if (s1 != null && lineHighlight == this.selectedLine && cursorPos >= 0) {
                int l3 = this.font.width(s1.substring(0, Math.max(Math.min(cursorPos, s1.length()), 0)));
                int i4 = l3 - this.font.width(s1) / 2;
                if (flag && cursorPos < s1.length()) {
                    pGuiGraphics.fill(i4, lineY - 1, i4 + 1, lineY + this.getTextLineHeight(), -16777216 | color);
                }

                if (selectionPos != cursorPos) {
                    int j4 = Math.min(cursorPos, selectionPos);
                    int j2 = Math.max(cursorPos, selectionPos);
                    int k2 = this.font.width(s1.substring(0, j4)) - this.font.width(s1) / 2;
                    int l2 = this.font.width(s1.substring(0, j2)) - this.font.width(s1) / 2;
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

    public static record WritableSignConfig(BlockState state, ConfiguredLine[] lines, int x, int y, int scale) {}
    public static record ConfiguredLine(String message, int xOffset, int yOffset, float minScale, float maxScale, int maxWidth, int color) {}
}
