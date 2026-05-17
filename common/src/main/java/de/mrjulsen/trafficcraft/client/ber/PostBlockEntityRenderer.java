package de.mrjulsen.trafficcraft.client.ber;

import com.mojang.math.Axis;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.RotatableBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.ber.SafeBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignPostBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.data.attachments.IPostAttachment;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.Map;

public class PostBlockEntityRenderer extends SafeBlockEntityRenderer<PostBlockEntity> {

    public PostBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BERGraphics<PostBlockEntity> graphics, float partialTick) {
        if (graphics.blockEntity() == null || graphics.blockEntity().isRemoved()) {
            return;
        }

        if (!(graphics.blockEntity().getBlockState().getBlock() instanceof TrafficSignPostBlock)) {
            return;
        }

        for (Map.Entry<Direction, IPostAttachment<?>> attachment : graphics.blockEntity().attachments.entrySet()) {
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0.5f, 0.5, 0.5f);
            graphics.poseStack().mulPose(graphics.blockEntity().getAttachmentRotation(attachment.getValue(), graphics.blockEntity().getBlockState().getValue(TrafficSignPostBlock.AXIS)));
            graphics.poseStack().mulPose(Axis.YP.rotationDegrees(180));
            graphics.poseStack().translate(-0.5f, 0.5f, -0.5f);
            graphics.poseStack().scale(DragonLib.BLOCK_PIXEL, -DragonLib.BLOCK_PIXEL, DragonLib.BLOCK_PIXEL);
            graphics.poseStack().pushPose();
            attachment.getValue().renderAdditional(graphics, partialTick);
            graphics.poseStack().popPose();
            graphics.poseStack().popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance() * 2;
    }
}
