package de.mrjulsen.trafficcraft.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.TrafficSignPostBlock;
import de.mrjulsen.trafficcraft.block.data.attachments.IPostAttachment;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.client.tooltip.ClientTrafficSignTooltipStack;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.util.VoxelShapeRotator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TrafficCraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

	public static final ResourceLocation POST_BASE = DLUtils.resourceLocation("modid", "block/post_base");

	@SubscribeEvent
	public static void onRegisterTooltipEvent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(TrafficSignTooltip.class, (tooltip) -> {
			return new ClientTrafficSignTooltipStack(tooltip);
		});
	}


	public static void onSalz(RenderHighlightEvent.Block event) {
		BlockHitResult hit = event.getTarget();
		if (hit.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos pos = hit.getBlockPos();
		Level level = Minecraft.getInstance().level;
		BlockState state = level.getBlockState(pos);
		if (level.getBlockState(pos).getBlock() instanceof TrafficSignPostBlock post && level.getBlockEntity(pos) instanceof PostBlockEntity be) {
			var poseStack = event.getPoseStack();
			var buffers = event.getMultiBufferSource();
			var camera = event.getCamera();
			Vec3 localPos = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

			for (Map.Entry<Direction, IPostAttachment<?>> entry : be.getAttachments().entrySet()) {
				IPostAttachment<?> attachment = entry.getValue();
				VoxelShape shape = VoxelShapeRotator.rotateByQuaternion(attachment.getShape(state, level, pos), be.getAttachmentRotation(attachment, state.getValue(TrafficSignPostBlock.AXIS)));
				boolean isHit = shape.toAabbs().stream()
						.map(bb -> bb.inflate(0.002))
						.anyMatch(bb -> bb.contains(localPos));

				if (isHit) {
					renderBoxes(poseStack, buffers, camera, pos, List.of(shape));
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	private static void renderBoxes(PoseStack poseStack, MultiBufferSource buffers, Camera camera, BlockPos pos, List<VoxelShape> boxes) {
		RenderType renderType = RenderType.lines();
		var buffer = buffers.getBuffer(renderType);
		float alpha = 0.4f;

		for (var box : boxes) {
			LevelRenderer.renderVoxelShape(poseStack, buffer, box,
					pos.getX() - camera.getPosition().x,
					pos.getY() - camera.getPosition().y,
					pos.getZ() - camera.getPosition().z,
					0, 0, 0,
					alpha, true);
		}
	}
}
