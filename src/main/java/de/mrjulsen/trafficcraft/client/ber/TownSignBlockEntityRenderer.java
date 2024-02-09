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
}
