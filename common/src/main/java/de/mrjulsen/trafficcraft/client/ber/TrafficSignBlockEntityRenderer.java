package de.mrjulsen.trafficcraft.client.ber;

import org.joml.Vector3f;

import com.mojang.math.Axis;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.RotatableBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficSignBlockEntityRenderer extends RotatableBlockEntityRenderer<TrafficSignBlockEntity> {

    public TrafficSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderBlock(BERGraphics<TrafficSignBlockEntity> graphics, float pPartialTick) {

        if (graphics.blockEntity() == null || graphics.blockEntity().isRemoved()) {
            return;
        }

        BlockState blockstate = graphics.blockEntity().getBlockState();
        if (blockstate == null) {
            return;
        }

        TrafficSignClientTexture tex = graphics.blockEntity().getClientTexture();

        if (tex.isDisposed()) {
            return;
        }

        double p = 1 / 16f;
        double z = blockstate.getValue(TrafficSignBlock.SHAPE) == TrafficSignShape.MISC ? 1.0d * p : 1.5d * p;
        graphics.poseStack().pushPose();
        graphics.poseStack().scale(16, 16, 16);
        graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
        graphics.poseStack().translate(-0.5d, -0.5d, z + 0.002d);
          
        RenderUtils.renderTexture(tex.getTextureLocation(), graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, blockstate.getValue(TrafficSignBlock.FACING), DLColor.WHITE, graphics.packedLight(), true);

        graphics.poseStack().popPose();
        
        if (tex.hasBackground()) {
            z = 9.0d * p - 0.5d;
            graphics.poseStack().pushPose();
            graphics.poseStack().scale(16, 16, 16);
            graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
            graphics.poseStack().mulPose(Axis.YP.rotationDegrees(180));
            graphics.poseStack().translate(-0.5d, -0.5d, -(p * 2) + z - 0.002d);
            
            RenderUtils.renderTexture(tex.getBackgroundTextureLocation(), graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, blockstate.getValue(TrafficSignBlock.FACING).getOpposite(), DLColor.WHITE, graphics.packedLight(), true);
            
            graphics.poseStack().popPose();
        }
    }
}
