package de.mrjulsen.trafficcraft.client.ber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
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
        pPoseStack.translate(pixel * 2.5f, pixel * 0.5f, pixel * 6);
        for (int i = 0; i < pBlockEntity.getColorSlotCount() && i < blockstate.getValue(TrafficLightBlock.MODEL).getLightsCount(); i++) {
            if (pBlockEntity.getColorOfSlot(i) != null && pBlockEntity.getEnabledColors().contains(pBlockEntity.getColorOfSlot(i))) {
                new TrafficLightTextureManager.TrafficLightTextureKey(pBlockEntity.getIcon(), pBlockEntity.getColorOfSlot(i)).render(pPoseStack, vertexconsumer);
            } else {
                new TrafficLightTextureManager.TrafficLightTextureKey(TrafficLightIcon.NONE, TrafficLightColor.NONE).render(pPoseStack, vertexconsumer);
            }
            pPoseStack.translate(0, pixel * 5, 0);
        }
        pPoseStack.popPose();
    }
}
