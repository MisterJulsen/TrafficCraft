package de.mrjulsen.trafficcraft.client.ber;

import java.util.BitSet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficSignBlockEntityRenderer implements BlockEntityRenderer<TrafficSignBlockEntity> {

    public TrafficSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    @SuppressWarnings("resource")
    public void render(TrafficSignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();

        if (pBlockEntity == null || blockstate == null) {
            return;
        }

        DynamicTexture tex = TrafficSignTextureCacheClient.getTexture(pBlockEntity, pBlockEntity.getTexture(), pBlockEntity.getBlockState().getValue(TrafficSignBlock.SHAPE) == TrafficSignShape.MISC, (texture) -> {
            pBlockEntity.setBase64Texture(TrafficSignTextureCacheClient.textureToBase64(pBlockEntity));
        });

        if (tex == null) {
            return;
        }

        ResourceLocation textureLocation = Minecraft.getInstance().textureManager.register("trafficsign_front", tex);
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.text(textureLocation));
        double p = 1 / 16f;
        double z = blockstate.getValue(TrafficSignBlock.SHAPE) == TrafficSignShape.MISC ? 9.0d * p - 0.5d : 9.5d * p - 0.5d;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.5f, 0.5f);

        float f4 = blockstate.getValue(TrafficSignBlock.FACING) == Direction.EAST || blockstate.getValue(TrafficSignBlock.FACING) == Direction.WEST ? blockstate.getValue(TrafficSignBlock.FACING).getOpposite().toYRot() : blockstate.getValue(TrafficSignBlock.FACING).toYRot();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
        pPoseStack.translate(-0.5d, -0.5d, z + 0.002d);
          
        
        addQuadSide(pBlockEntity, blockstate, blockstate.getValue(TrafficSignBlock.FACING), vertexconsumer, pPoseStack,
            0, 0, 0,
            1, 1, 0,
            0, 0,
            1, 1,
            1.0F, 1.0F, 1.0F, 1.0F,
            pPackedLight
        );
        

        pPoseStack.popPose();
        
        if (TrafficSignTextureCacheClient.hasBackground(pBlockEntity)) {
            textureLocation = Minecraft.getInstance().textureManager.register("trafficsign_back", TrafficSignTextureCacheClient.getBackground(pBlockEntity));
            vertexconsumer = pBufferSource.getBuffer(RenderType.text(textureLocation));
            z = 7.0d * p - 0.5d;
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4)); 
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180));         
            pPoseStack.translate(-0.5d, -0.5d, z - 0.002d);
            
            addQuadSide(pBlockEntity, blockstate, blockstate.getValue(TrafficSignBlock.FACING), vertexconsumer, pPoseStack,
                0, 0, 0,
                1, 1, 0,
                0, 0,
                1, 1,
                1.0F, 1.0F, 1.0F, 1.0F,
                pPackedLight
            );
            pPoseStack.popPose();
        }
    }

    public static void addVert(VertexConsumer builder, PoseStack pPoseStack, float x, float y, float z, float u, float v, float r, float g, float b, float a, int lu, int lv) {
        builder.vertex(pPoseStack.last().pose(), x, y, z).color(r, g, b, a).uv(u, v).uv2(lu, lv).overlayCoords(OverlayTexture.NO_OVERLAY).normal(pPoseStack.last().normal(), 0, 0, 1).endVertex();
    }

    @SuppressWarnings("resource")
    public static void addQuadSide(BlockEntity be, BlockState state, Direction direction, VertexConsumer builder, PoseStack pPoseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, float r, float g, float b, float a, int packedLight) {

        if (Minecraft.getInstance().options.ambientOcclusion == AmbientOcclusionStatus.OFF || be.getLevel() == null || be.getBlockPos() == null) {
            addVert(builder, pPoseStack, x0, y1, z0, u0, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
            addVert(builder, pPoseStack, x0, y0, z0, u0, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
            addVert(builder, pPoseStack, x1, y0, z1, u1, v1, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
            addVert(builder, pPoseStack, x1, y1, z1, u1, v0, r, g, b, a, packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        } else {
            try {
                float[] afloat = new float[Direction.values().length * 2];
                BitSet bitset = new BitSet(3);
                ModelBlockRenderer.AmbientOcclusionFace ao = Minecraft.getInstance().getBlockRenderer().getModelRenderer().new AmbientOcclusionFace();

                BlockPos origin = be.getLevel().getChunk(be.getBlockPos()).getPos().getWorldPosition();
                BlockAndTintGetter batg = new RenderRegionCache().createRegion(be.getLevel(), origin.offset(-1, -1, -1), origin.offset(16, 16, 16), 1);
                if (batg == null) {
                    ModMain.LOGGER.warn("Chunk Region Renderer was null.");
                    return;
                }
                ao.calculate(batg, state, be.getBlockPos(), direction, afloat, bitset, true);
                
                addVert(builder, pPoseStack, x0, y1, z0, u0, v0, r * ao.brightness[0], g * ao.brightness[0], b * ao.brightness[0], a, ao.lightmap[0] & 0xFFFF, (ao.lightmap[0] >> 16) & 0xFFFF);
                addVert(builder, pPoseStack, x0, y0, z0, u0, v1, r * ao.brightness[1], g * ao.brightness[1], b * ao.brightness[1], a, ao.lightmap[1] & 0xFFFF, (ao.lightmap[1] >> 16) & 0xFFFF);
                addVert(builder, pPoseStack, x1, y0, z1, u1, v1, r * ao.brightness[2], g * ao.brightness[2], b * ao.brightness[2], a, ao.lightmap[2] & 0xFFFF, (ao.lightmap[2] >> 16) & 0xFFFF);
                addVert(builder, pPoseStack, x1, y1, z1, u1, v0, r * ao.brightness[3], g * ao.brightness[3], b * ao.brightness[3], a, ao.lightmap[3] & 0xFFFF, (ao.lightmap[3] >> 16) & 0xFFFF);
            } catch (Exception e) {
                ModMain.LOGGER.error("Error while rendering Traffic Sign with AO.", e);
            }
        }

    }
}
