package de.mrjulsen.trafficcraft.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.WritableTrafficSign;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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

        ResourceLocation textureLocation = Minecraft.getInstance().textureManager.register("salz", pBlockEntity.getDynamicTexture());
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.text(textureLocation));
        double p = 1 / 16f;
        double z = 9.5d * p - 0.5d;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);

        float f4 = blockstate.getValue(WritableTrafficSign.FACING) == Direction.EAST || blockstate.getValue(WritableTrafficSign.FACING) == Direction.WEST ? blockstate.getValue(WritableTrafficSign.FACING).getOpposite().toYRot() : blockstate.getValue(WritableTrafficSign.FACING).toYRot();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
         
        pPoseStack.translate(-0.5d, -0.5d, z + 0.001d);
           
        addQuadSide(vertexconsumer, pPoseStack,
            0, 0, 0,
            1, 1, 0,
            0, 0,
            1, 1,
            255, 255, 255, 255,
            (int)(pPackedLight * 0.8f),
            pPackedOverlay
        );
        pPoseStack.popPose();
    }

    public static void addVert(VertexConsumer builder, PoseStack pPoseStack, float x, float y, float z, float u, float v, int r, int g, int b, int a, int light, int pPackedOverlay) {
        builder.vertex(pPoseStack.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).uv2(light).overlayCoords(pPackedOverlay).normal(pPoseStack.last().normal(), 0, 0, 1).endVertex();
    }

    public static void addQuadSide(VertexConsumer builder, PoseStack pPoseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int r, int g, int b, int a, int light, int pPackedOverlay) {
        addVert(builder, pPoseStack, x0, y0, z0, u0, v1, r, g, b, a, light, pPackedOverlay);
        addVert(builder, pPoseStack, x1, y0, z1, u1, v1, r, g, b, a, light, pPackedOverlay);
        addVert(builder, pPoseStack, x1, y1, z1, u1, v0, r, g, b, a, light, pPackedOverlay);
        addVert(builder, pPoseStack, x0, y1, z0, u0, v0, r, g, b, a, light, pPackedOverlay);
    }
}
