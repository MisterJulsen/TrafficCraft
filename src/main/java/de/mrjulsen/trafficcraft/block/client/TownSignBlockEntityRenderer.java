package de.mrjulsen.trafficcraft.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class TownSignBlockEntityRenderer implements BlockEntityRenderer<TownSignBlockEntity> {
    private final Font font;

    public TownSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(TownSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();
        double currentY = 0;
        for (int i1 = 0; i1 < pBlockEntity.lineCount(); ++i1) {
            String line = pBlockEntity.getText(i1);
            if (line == null || this.font.width(line) == 0)
                continue;

            pPoseStack.pushPose();

            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            float f4 = -blockstate.getValue(WritableTrafficSign.FACING).toYRot();
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
            pPoseStack.translate(0.0D, 0.4D, 0.1D);
            double lineHeight = this.font.lineHeight;

            if (i1 == 1) {
                int defScale = 2;
                double scale = calcScale(0.01D, 0.01D * defScale, 90.0D / defScale, this.font.width(line));
                lineHeight *= 3;
                pPoseStack.scale((float)scale, (float)-scale, (float)scale);                
            } else {
                pPoseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
            }

            float f3 = (float) (-this.font.width(line) / 2);
            this.font.drawInBatch(line, f3, (float)currentY, DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, false, 0, pPackedLight);
                        
            pPoseStack.popPose();

            currentY += lineHeight;
        }
    }

    public double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }
}
