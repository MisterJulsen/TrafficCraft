package de.mrjulsen.trafficcraft.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class TownSignBlockEntityRenderer extends WritableSignBlockEntityRenderer<TownSignBlockEntity> {
    
    public TownSignBlockEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(TownSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        switch (pBlockEntity.getBlockState().getValue(TownSignBlock.VARIANT)) {
            case FRONT:
                renderInternal(pBlockEntity.getRenderConfig(), pBlockEntity, pBlockEntity::getText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, false);
                break;
            case BACK:
                renderInternal(pBlockEntity.getBackRenderConfig(), pBlockEntity, pBlockEntity::getBackText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, false);
                break;
            case BOTH:
                renderInternal(pBlockEntity.getRenderConfig(), pBlockEntity, pBlockEntity::getText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, false);
                renderInternal(pBlockEntity.getBackRenderConfig(), pBlockEntity, pBlockEntity::getBackText, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, false);
                break;
        }
    }

    /*
    public void renderFront(TownSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        
        BlockState blockstate = pBlockEntity.getBlockState();
        double currentY = 0;
        
        for (int i1 = 0; i1 < pBlockEntity.getRenderConfig().lineData().length; ++i1) {
            String line = pBlockEntity.getText(i1);
            if (line == null || this.font.width(line) == 0)
                continue;

            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            float f4 = -blockstate.getValue(WritableTrafficSign.FACING).toYRot();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(f4));
            pPoseStack.translate(0.0D, 0.4D, 0.1D);
            double lineHeight = this.font.lineHeight;

            if (i1 == 1) {
                int defScale = 2;
                double scale = 1;//config.calcScale(0.01D, 0.01D * defScale, 90.0D / defScale, this.font.width(line));
                lineHeight *= 3;
                pPoseStack.scale((float)scale, (float)-scale, (float)scale);                
            } else {
                pPoseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
            }

            float f3 = (float) (-this.font.width(line) / 2);
            this.font.drawInBatch(line, f3, (float)currentY, DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);
                        
            pPoseStack.popPose();

            currentY += lineHeight;
        }
    }

    public void renderBack(TownSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, boolean bothSides) {
        WritableSignConfig config = pBlockEntity.getTownSignRenderConfig(ETownSignSide.BACK);
        BlockState blockState = pBlockEntity.getBlockState();
        final float scale = 1.0F / config.scale();

        for (int lineIndex = 0; lineIndex < config.lineData().length; ++lineIndex) {
            String line = pBlockEntity.getBackText(lineIndex);
            if (line == null || this.font.width(line) == 0)
                continue;
                
            ConfiguredLineData data = config.lineData()[lineIndex];
            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 0.5f, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(config.blockEntityRendererRotation().apply(blockState)));
            pPoseStack.translate(0, 0, config.berZ());
            float xCenter = (float)(-this.font.width(line) / 2);

            Vector3f vector3f = config.berTextScale(line, font, scale, data);              
            pPoseStack.scale(scale, -scale, scale);
            pPoseStack.translate(data.xOffset() / scale, data.yOffset() / scale - (WritableSignScreen.DEFAULT_LINE_HEIGHT / 2 * config.lineData()[0].lineHeightScale()) + config.getLineHeightsUntil(lineIndex) + config.getLineOffset(lineIndex, vector3f.y), 0);   
            pPoseStack.scale(vector3f.x(), vector3f.y(), vector3f.z());
            this.font.drawInBatch(line, xCenter, 0, DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);
                        
            pPoseStack.popPose();
        }
    }
    */
}
