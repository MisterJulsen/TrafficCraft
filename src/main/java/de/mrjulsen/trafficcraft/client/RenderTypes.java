package de.mrjulsen.trafficcraft.client;

import java.io.IOException;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.config.ERenderType;
import de.mrjulsen.trafficcraft.config.ModClientConfig;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @see https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/foundation/render/RenderTypes.java
 */
@EventBusSubscriber(Dist.CLIENT)
public class RenderTypes extends RenderStateShard {

   @SubscribeEvent
   public static void renderCustomRT(RenderLevelStageEvent event) {
      if (ModClientConfig.GLOW_RENDER_EFFECT.get() == ERenderType.DEFAULT) {
         renderChunkLayer(event.getLevelRenderer(), event.getFrustum(), RenderTypes.GLOW_SOLID, event.getPoseStack(),
            event.getCamera().getPosition().x, event.getCamera().getPosition().y, event.getCamera().getPosition().z,
            event.getProjectionMatrix());
      }
   }

   public static void renderChunkLayer(LevelRenderer renderer, Frustum frustum, RenderType pRenderType,
         PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, Matrix4f pProjectionMatrix) {
      RenderSystem.assertOnRenderThread();
      pRenderType.setupRenderState();

      Minecraft.getInstance().getProfiler().push("filterempty");
      Minecraft.getInstance().getProfiler().popPush(() -> {
         return "render_" + pRenderType;
      });
      boolean flag = pRenderType != RenderType.translucent();
      ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = renderer.renderChunksInFrustum
            .listIterator(flag ? 0 : renderer.renderChunksInFrustum.size());
      VertexFormat vertexformat = pRenderType.format();
      ShaderInstance shaderinstance = RenderSystem.getShader();
      BufferUploader.reset();

      for (int k = 0; k < 12; ++k) {
         int i = RenderSystem.getShaderTexture(k);
         shaderinstance.setSampler("Sampler" + k, i);
      }

      if (shaderinstance.MODEL_VIEW_MATRIX != null) {
         shaderinstance.MODEL_VIEW_MATRIX.set(pPoseStack.last().pose());
      }

      if (shaderinstance.PROJECTION_MATRIX != null) {
         shaderinstance.PROJECTION_MATRIX.set(pProjectionMatrix);
      }

      if (shaderinstance.COLOR_MODULATOR != null) {
         shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
      }

      if (shaderinstance.FOG_START != null) {
         shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
      }

      if (shaderinstance.FOG_END != null) {
         shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
      }

      if (shaderinstance.FOG_COLOR != null) {
         shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
      }

      if (shaderinstance.FOG_SHAPE != null) {
         shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
      }

      if (shaderinstance.TEXTURE_MATRIX != null) {
         shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
      }

      if (shaderinstance.GAME_TIME != null) {
         shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
      }

      RenderSystem.setupShaderLights(shaderinstance);
      shaderinstance.apply();
      Uniform uniform = shaderinstance.CHUNK_OFFSET;
      boolean flag1 = false;

      while (true) {
         if (flag) {
            if (!objectlistiterator.hasNext()) {
               break;
            }
         } else if (!objectlistiterator.hasPrevious()) {
            break;
         }

         LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = flag ? objectlistiterator.next()
               : objectlistiterator.previous();
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo1.chunk;
         if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().isEmpty(pRenderType)) {
            VertexBuffer vertexbuffer = chunkrenderdispatcher$renderchunk.getBuffer(pRenderType);
            BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
            if (uniform != null) {
               uniform.set((float) ((double) blockpos.getX() - pCamX), (float) ((double) blockpos.getY() - pCamY),
                     (float) ((double) blockpos.getZ() - pCamZ));
               uniform.upload();
            }

            vertexbuffer.drawChunkLayer();
            flag1 = true;
         }
      }

      if (uniform != null) {
         uniform.set(Vector3f.ZERO);
      }

      shaderinstance.clear();
      if (flag1) {
         vertexformat.clearBufferState();
      }

      VertexBuffer.unbind();
      VertexBuffer.unbindVertexArray();
      Minecraft.getInstance().getProfiler().pop();
      pRenderType.clearRenderState();
   }

   public static RenderType getGlowingSolid() {
      return RenderType.create(createLayerName("glowing_solid"), DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
            256,
            true, false, RenderType.CompositeState.builder()
                  .setLightmapState(LIGHTMAP)
                  .setShaderState(new RenderStateShard.ShaderStateShard(Shaders::getGlowingShader))
                  .setTextureState(BLOCK_SHEET_MIPPED)
                  .createCompositeState(true));
   }

   public static final RenderType GLOW_SOLID = getGlowingSolid();

   static {
      if (ModClientConfig.GLOW_RENDER_EFFECT.get() == ERenderType.REPLACE_VANILLA) {
         RenderType.SOLID = GLOW_SOLID;
      }
   }

   private static String createLayerName(String name) {
      return ModMain.MOD_ID + ":" + name;
   }

   // Mmm gimme those protected fields
   private RenderTypes() {
      super(null, null, null);
   }

   @EventBusSubscriber(modid = ModMain.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
   public static class Shaders {
      private static ShaderInstance glowingShader;

      @SubscribeEvent
      public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
         ResourceManager resourceManager = event.getResourceManager();
         event.registerShader(new ShaderInstance(resourceManager,
               new ResourceLocation(ModMain.MOD_ID, "rendertype_glow_solid"), DefaultVertexFormat.BLOCK), shader -> {
                  glowingShader = shader;
               });

         ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAINT_BUCKET.get(), RenderType.cutout());
         ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE.get(), RenderType.cutout());
         ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE_COVER.get(), RenderType.cutout());
         ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_SIGN_WORKBENCH.get(), RenderType.cutout());
         ItemBlockRenderTypes.setRenderLayer(ModBlocks.WHITE_DELINEATOR.get(), ModClientConfig.GLOW_RENDER_EFFECT.get() == ERenderType.DEFAULT ? GLOW_SOLID : RenderType.solid());

      }

      public static ShaderInstance getGlowingShader() {
         return glowingShader;
      }
   }

}
