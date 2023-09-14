package de.mrjulsen.trafficcraft.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficSignBlockEntityRenderer implements BlockEntityRenderer<TrafficSignBlockEntity> {

    public TrafficSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    @SuppressWarnings("resource")
    public void render(TrafficSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();
        if (pBlockEntity.getDynamicTexture() == null) {
            return;
        }

        int lu = pPackedLight & '\uffff';
        int lv = pPackedLight >> 16 & '\uffff';

        ResourceLocation textureLocation = Minecraft.getInstance().textureManager.register("trafficsign_front", pBlockEntity.getDynamicTexture());
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.text(textureLocation));
        double p = 1 / 16f;
        double z = blockstate.getValue(TrafficSignBlock.SHAPE) == TrafficSignShape.MISC ? 9.0d * p - 0.5d : 9.5d * p - 0.5d;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);

        float f4 = blockstate.getValue(TrafficSignBlock.FACING) == Direction.EAST || blockstate.getValue(TrafficSignBlock.FACING) == Direction.WEST ? blockstate.getValue(TrafficSignBlock.FACING).getOpposite().toYRot() : blockstate.getValue(TrafficSignBlock.FACING).toYRot();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));         
        pPoseStack.translate(-0.5d, -0.5d, z + 0.002d);
           
        addQuadSide(vertexconsumer, pPoseStack,
            0, 0, 0,
            1, 1, 0,
            0, 0,
            1, 1,
            255, 255, 255, 255,
            lu, lv
        );
        pPoseStack.popPose();
        
        if (pBlockEntity.hasBackground()) {
            textureLocation = Minecraft.getInstance().textureManager.register("trafficsign_back", pBlockEntity.getBackground());
            vertexconsumer = pBufferSource.getBuffer(RenderType.text(textureLocation));
            z = 7.0d * p - 0.5d;
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4)); 
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180));         
            pPoseStack.translate(-0.5d, -0.5d, z - 0.002d);
            
            addQuadSide(vertexconsumer, pPoseStack,
                0, 0, 0,
                1, 1, 0,
                0, 0,
                1, 1,
                1, 1, 1, 1,
                lu, lv
            );
            pPoseStack.popPose();
        }
    }

    public static void addVert(VertexConsumer builder, PoseStack pPoseStack, float x, float y, float z, float u, float v, int r, int g, int b, int a, int lu, int lv) {
        builder.vertex(pPoseStack.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).uv2(lu, lv).overlayCoords(OverlayTexture.NO_OVERLAY).normal(pPoseStack.last().normal(), 0, 0, 1).endVertex();
    }

    public static void addQuadSide(VertexConsumer builder, PoseStack pPoseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int r, int g, int b, int a, int lu, int lv) {
        addVert(builder, pPoseStack, x0, y0, z0, u0, v1, r, g, b, a, lu, lv);
        addVert(builder, pPoseStack, x1, y0, z1, u1, v1, r, g, b, a, lu, lv);
        addVert(builder, pPoseStack, x1, y1, z1, u1, v0, r, g, b, a, lu, lv);
        addVert(builder, pPoseStack, x0, y1, z0, u0, v0, r, g, b, a, lu, lv);
    }
}
