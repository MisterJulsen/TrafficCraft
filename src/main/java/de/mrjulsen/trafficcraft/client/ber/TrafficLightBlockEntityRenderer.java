package de.mrjulsen.trafficcraft.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.TrafficLightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightBlockEntityRenderer implements BlockEntityRenderer<TrafficLightBlockEntity> {

    public BlockRenderDispatcher blockRenderDispatcher;

    public TrafficLightBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TrafficLightBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();
        pPoseStack.pushPose();        
        
        pPoseStack.translate(0.5f, 0, 0.5f);
        float f4 = blockstate.getValue(TrafficSignBlock.FACING) == Direction.EAST || blockstate.getValue(TrafficSignBlock.FACING) == Direction.WEST ? blockstate.getValue(TrafficSignBlock.FACING).getOpposite().getClockWise().toYRot() : blockstate.getValue(TrafficSignBlock.FACING).getClockWise().toYRot();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
        pPoseStack.translate(-0.5f, 0, -0.5f);

        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.solid());
        final float pixel = 1.0F / 16.0F;
        TrafficLightTexture.GREEN.render(pPoseStack, vertexconsumer);
        pPoseStack.translate(0, pixel * 6, 0);
        TrafficLightTexture.YELLOW.render(pPoseStack, vertexconsumer);
        pPoseStack.translate(0, pixel * 6, 0);
        TrafficLightTexture.RED.render(pPoseStack, vertexconsumer);

        //final float pixel = 1.0f / 16.0f;
        //TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ETrafficLightTexture.STRAIGHT_RIGHT_RED.getTextureLocation());
        //vertexconsumer.putBulkData(pPoseStack.last(), ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 16, Transformation.identity(), sprite), 1, 1, 1, LightTexture.FULL_BRIGHT, 0);
        //vertexconsumer.putBulkData(pPoseStack.last(), ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(0, 0, 0), 16, 1, Transformation.identity(), sprite), 1, 1, 1, LightTexture.FULL_BRIGHT, 0);
        //vertexconsumer.putBulkData(pPoseStack.last(), ClientTools.createQuad(new Vector3f(0, 0, pixel * 4), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 1, Transformation.identity(), sprite), 1, 1, 1, LightTexture.FULL_BRIGHT, 0);
        //vertexconsumer.putBulkData(pPoseStack.last(), ClientTools.createQuad(new Vector3f(0, 0, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(0, 0, pixel * 4), 16, 1, Transformation.identity(), sprite), 1, 1, 1, LightTexture.FULL_BRIGHT, 0);
        //vertexconsumer.putBulkData(pPoseStack.last(), ClientTools.createQuad(new Vector3f(0, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(0, pixel * 4, 0), 16, 1, Transformation.identity(), sprite), 1, 1, 1, LightTexture.FULL_BRIGHT, 0);
        
        pPoseStack.popPose();
    }
}
