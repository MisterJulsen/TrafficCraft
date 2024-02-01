package de.mrjulsen.trafficcraft.client.ber;

import java.util.function.Function;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import de.mrjulsen.trafficcraft.block.entity.WritableTrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.trafficcraft.client.screen.WritableSignScreen.WritableSignConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class WritableSignBlockEntityRenderer<T extends WritableTrafficSignBlockEntity> implements BlockEntityRenderer<T> {
    protected final Font font;

    public WritableSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        WritableSignConfig config = pBlockEntity.getRenderConfig();        
        renderInternal(config, pBlockEntity, pBlockEntity::getText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, false);
        if (config.renderBack()) {
            renderInternal(config, pBlockEntity, pBlockEntity::getText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, true);
        }
    }

    protected void renderInternal(WritableSignConfig config, T pBlockEntity, Function<Integer, String> getText, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, boolean isOpposite) {
        BlockState blockState = pBlockEntity.getBlockState();
        final float scale = 1.0F / config.scale();

        for (int lineIndex = 0; lineIndex < config.lineData().length; ++lineIndex) {
            String line = getText.apply(lineIndex);
            if (line == null || this.font.width(line) == 0)
                continue;
                
            ConfiguredLineData data = config.lineData()[lineIndex];
            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 0.5f, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(config.blockEntityRendererRotation().apply(blockState) + (isOpposite ? 180 : 0)));
            pPoseStack.translate((isOpposite ? -1 : 1) * config.berX(), config.berY(), config.berZ());
            float xCenter = (float)(-this.font.width(line) / 2);

            Vector3f vector3f = config.berTextScale(line, font, scale, data);              
            pPoseStack.scale(scale, -scale, scale);
            pPoseStack.translate(data.xOffset() / scale, data.yOffset() / scale - (WritableSignScreen.DEFAULT_LINE_HEIGHT / 2 * config.lineData()[0].lineHeightScale()) + config.getLineHeightsUntil(lineIndex) + config.getLineOffset(lineIndex, vector3f.y), 0);   
            pPoseStack.scale(vector3f.x(), vector3f.y(), vector3f.z());
            this.font.drawInBatch(line, xCenter, 0, config.berColor(), false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);
                        
            pPoseStack.popPose();
        }
    }
}
