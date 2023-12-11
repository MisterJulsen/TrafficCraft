package de.mrjulsen.trafficcraft.client.ber;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.ETrafficLightTexture;
import de.mrjulsen.trafficcraft.client.TrafficSignTextureCacheClient;
import de.mrjulsen.trafficcraft.util.ClientTools;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder.Direct;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;

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
        ETrafficLightTexture.GREEN.render(pPoseStack, vertexconsumer);
        pPoseStack.translate(0, pixel * 6, 0);
        ETrafficLightTexture.YELLOW.render(pPoseStack, vertexconsumer);
        pPoseStack.translate(0, pixel * 6, 0);
        ETrafficLightTexture.RED.render(pPoseStack, vertexconsumer);

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
