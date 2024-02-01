package de.mrjulsen.trafficcraft.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.data.PaintColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class StreetSignBlockEntityRenderer implements BlockEntityRenderer<StreetSignBlockEntity> {
    private final Font font;

    public StreetSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(StreetSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();
        double currentY = 0;
        for (int i1 = 0; i1 < pBlockEntity.getRenderConfig().lineData().length; ++i1) {
            String line = pBlockEntity.getText(i1);
            if (line == null || this.font.width(line) == 0)
                continue;

            pPoseStack.pushPose();

            double lineHeight = this.font.lineHeight;
            double defScale = 1.5D;
            double scale = calcScale(0.01D, 0.01D * defScale, 95.0D / defScale, this.font.width(line)); 
            lineHeight *= 3;
            float f3 = (float) (-this.font.width(line) / 2);

            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            float f4 = -blockstate.getValue(WritableTrafficSign.FACING).getClockWise(Axis.Y).toYRot();
            pPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(f4));
            pPoseStack.translate(-0.4D, 0.34D, 0.02D);
            pPoseStack.scale((float)scale, -0.015F, 0.015F);
            this.font.drawInBatch(line, f3, (float)currentY, PaintColor.useWhiteOrBlackForeColor(pBlockEntity.getColor().getTextureColor()) ? DyeColor.WHITE.getTextColor() : DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);
            pPoseStack.popPose();
         
            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            float f5 = -blockstate.getValue(WritableTrafficSign.FACING).getClockWise(Axis.Y).getOpposite().toYRot();
            pPoseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(f5));
            pPoseStack.translate(0.4D, 0.34D, 0.02D);
            pPoseStack.scale((float)scale, -0.015F, 0.015F);
            this.font.drawInBatch(line, f3, (float)currentY, PaintColor.useWhiteOrBlackForeColor(pBlockEntity.getColor().getTextureColor()) ? DyeColor.WHITE.getTextColor() : DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);

            pPoseStack.popPose();

            currentY += lineHeight;
        }
    }

    public double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }
}
