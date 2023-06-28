package de.mrjulsen.trafficcraft.block.client;

import com.electronwill.nightconfig.core.io.WritingMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.entity.HouseNumberSignBlockEntity;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class HouseNumberSignBlockEntityRenderer implements BlockEntityRenderer<HouseNumberSignBlockEntity> {
    private final Font font;

    public HouseNumberSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(HouseNumberSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();
        double currentY = 0;
        for (int i1 = 0; i1 < pBlockEntity.getRenderingConfig().getLines(); ++i1) {
            String line = pBlockEntity.getText(i1);
            if (line == null || this.font.width(line) == 0)
                continue;

            pPoseStack.pushPose();

            double lineHeight = this.font.lineHeight;
            double defScale = 3;
            double scale = calcScale(0.01D, 0.01D * defScale, 50.0D / defScale, this.font.width(line)); 
            lineHeight *= 3;
            float f3 = (float) (-this.font.width(line) / 2);

            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            float f4 = blockstate.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockstate.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockstate.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockstate.getValue(WritableTrafficSign.FACING).toYRot();
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
            pPoseStack.translate(0, -0.05D + (scale * 5), -0.45D);
            pPoseStack.scale((float)scale, (float)-scale, (float)scale);
            this.font.drawInBatch(line, f3, (float)currentY, PaintColor.useWhiteOrBlackForeColor(pBlockEntity.getColor().getTextureColor()) ? DyeColor.WHITE.getTextColor() : DyeColor.BLACK.getTextColor(), false, pPoseStack.last().pose(), pBufferSource, false, 0, pPackedLight);
            pPoseStack.popPose();

            currentY += lineHeight;
        }
    }

    public double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
        double scale = Math.min(maxWidth / fontWidth, 1.0D);
        return Math.max(maxScale * scale, minScale);
    }
}
