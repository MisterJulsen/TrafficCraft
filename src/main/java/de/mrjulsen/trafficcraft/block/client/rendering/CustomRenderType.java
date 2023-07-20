package de.mrjulsen.trafficcraft.block.client.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.RenderType;

public class CustomRenderType extends RenderType {

    //Ignored
    private CustomRenderType(String name, VertexFormat format, Mode drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, runnablePre, runnablePost);
    }

    public static final RenderType GLOW = create("glow", DefaultVertexFormat.BLOCK, Mode.QUADS, 2097152, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(CustomShader.GLOW.shard)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setLightmapState(LIGHTMAP)
            .createCompositeState(true)
    );
}
