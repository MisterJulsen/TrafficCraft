package de.mrjulsen.trafficcraft.client.ber;

import java.util.function.Function;

import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.block.WritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.ber.SafeBlockEntityRenderer;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen.ConfiguredLineData;
import de.mrjulsen.mcdragonlib.client.builtin.WritableSignScreen.WritableSignConfig;
import de.mrjulsen.mcdragonlib.client.util.BERUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class WritableSignBlockEntityRenderer<T extends WritableSignBlockEntity> extends SafeBlockEntityRenderer<T> {
    protected final Font font;

    public WritableSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
    }

    @Override
    public void renderSafe(BERGraphics<T> graphics, float pPartialTick) {
        WritableSignConfig config = graphics.blockEntity().getRenderConfig();        
        renderInternal(config, graphics.blockEntity()::getText, pPartialTick, graphics, false);
        if (config.renderBack()) {
            renderInternal(config, graphics.blockEntity()::getText, pPartialTick, graphics, true);
        }
    }

    protected void renderInternal(WritableSignConfig config, Function<Integer, String> getText, float pPartialTick, BERGraphics<T> graphics, boolean isOpposite) {
        BlockState blockState = graphics.blockEntity().getBlockState();
        final float scale = 1.0F / config.scale();

        for (int lineIndex = 0; lineIndex < config.lineData().length; ++lineIndex) {
            String line = getText.apply(lineIndex);
            if (line == null || this.font.width(line) == 0)
                continue;
                
            ConfiguredLineData data = config.lineData()[lineIndex];
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0.5D, 0.5f, 0.5F);
            graphics.poseStack().mulPose(Vector3f.YP.rotationDegrees(config.blockEntityRendererRotation().apply(blockState) + (isOpposite ? 180 : 0)));
            graphics.poseStack().translate((isOpposite ? -1 : 1) * config.berX(), config.berY(), config.berZ());
            float xCenter = (float)(-this.font.width(line) / 2);

            Vector3f vector3f = config.berTextScale(line, font, scale, data);              
            graphics.poseStack().scale(scale, -scale, scale);
            graphics.poseStack().translate(data.xOffset() / scale, data.yOffset() / scale - (WritableSignScreen.DEFAULT_LINE_HEIGHT / 2 * config.lineData()[0].lineHeightScale()) + config.getLineHeightsUntil(lineIndex) + config.getLineOffset(lineIndex, vector3f.y()), 0);   
            graphics.poseStack().scale(vector3f.x(), vector3f.y(), vector3f.z());
            BERUtils.drawString(graphics, font, xCenter, 0, line, config.berColor(), EAlignment.LEFT, false);
                        
            graphics.poseStack().popPose();
        }
    }
}
