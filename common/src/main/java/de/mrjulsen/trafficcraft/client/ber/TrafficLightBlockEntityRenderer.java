package de.mrjulsen.trafficcraft.client.ber;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.RotatableBlockEntityRenderer;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightBlockEntityRenderer extends RotatableBlockEntityRenderer<TrafficLightBlockEntity> {

    public BlockRenderDispatcher blockRenderDispatcher;

    public TrafficLightBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance() * 2;
    }

    @Override
    public void renderBlock(BERGraphics<TrafficLightBlockEntity> graphics, float pPartialTick) {
        BlockState blockstate = graphics.blockEntity().getBlockState();
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(6f, 5.5f, 13);
        for (int i = 0; i < graphics.blockEntity().getColorSlotCount() && i < blockstate.getValue(TrafficLightBlock.MODEL).getLightsCount(); i++) {
            if (graphics.blockEntity().getColorOfSlot(i) != null && graphics.blockEntity().isColorEnabled(graphics.blockEntity().getColorOfSlot(i), true)) {
                new TrafficLightTextureManager.TrafficLightTextureKey(graphics.blockEntity().getIcon(), graphics.blockEntity().getColorOfSlot(i)).render(graphics, graphics.blockEntity(), graphics.packedLight());
            } else {
                new TrafficLightTextureManager.TrafficLightTextureKey(TrafficLightIcon.NONE, TrafficLightColor.NONE).render(graphics, graphics.blockEntity(), graphics.packedLight());
            }
            graphics.poseStack().translate(0, 5, 0);
        }
        graphics.poseStack().popPose();
    }
}
