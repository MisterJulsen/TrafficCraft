package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.lang.model.type.ErrorType;

import com.google.common.collect.BiMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.util.ClientTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class TrafficLightTextureManager {
    private static final Map<TrafficLightTextureKey, Model> models = new HashMap<>();

    static {
        Arrays.stream(TrafficLightIcon.values())
            .forEach(
                x -> Arrays.stream(TrafficLightColor.values())
                    .filter(y -> x == TrafficLightIcon.NONE || y != TrafficLightColor.NONE)
                    .forEach(y -> {
                        TrafficLightTextureKey key = new TrafficLightTextureKey(x, y);
                        models.put(key, Model.create(key));
                    })                    
            );
    }

    private static final String TEXTURE_PATH = "block/traffic_light";

    public static ResourceLocation getTextureLocation(TrafficLightIcon icon, TrafficLightColor color) {
        return getTextureLocation(new TrafficLightTextureKey(icon, color));
    }

    public static ResourceLocation getTextureLocation(TrafficLightTextureKey key) {
        if (key.isOffState()) {
            return new ResourceLocation(ModMain.MOD_ID, String.format("%s/off", TEXTURE_PATH));
        }
        return new ResourceLocation(ModMain.MOD_ID, String.format("%s/%s_%s", TEXTURE_PATH, key.getIcon().getName(), key.getColor().getName()));
    }

    public static TextureAtlasSprite getSprite(TrafficLightIcon icon, TrafficLightColor color) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureLocation(icon, color));
    }

    public static void render(PoseStack poseStack, VertexConsumer consumer, TrafficLightIcon icon, TrafficLightColor color) {
        render(poseStack, consumer, new TrafficLightTextureKey(icon, color));
    }

    public static void render(PoseStack poseStack, VertexConsumer consumer, TrafficLightTextureKey key) {
        models.get(key).render(poseStack, consumer);
    }

    public static class TrafficLightTextureKey {
        private final TrafficLightIcon icon;
        private final TrafficLightColor color;

        public TrafficLightTextureKey(TrafficLightIcon icon, TrafficLightColor color) {
            this.icon = icon;
            this.color = color;
        }

        public TrafficLightIcon getIcon() {
            return icon;
        }

        public TrafficLightColor getColor() {
            return color;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TrafficLightTextureKey other) {
                return getIcon() == other.getIcon() && getColor() == other.getColor();
            }
            return false;
        }

        public ResourceLocation getTextureLocation() {
            return TrafficLightTextureManager.getTextureLocation(getIcon(), getColor());
        }

        public TextureAtlasSprite getSprite() {
            return TrafficLightTextureManager.getSprite(getIcon(), getColor());
        }

        public void render(PoseStack poseStack, VertexConsumer consumer) {
            TrafficLightTextureManager.render(poseStack, consumer, this);
        }

        public boolean isOffState() {
            return getIcon() == TrafficLightIcon.NONE && getColor() == TrafficLightColor.NONE;
        }
    }

    public static class Model {
        private static final float pixel = 1.0F / 16.0F;

        private final List<BakedQuad> quads = new ArrayList<>();
        private final TrafficLightTextureKey key;

        private Model(List<BakedQuad> quads, TrafficLightTextureKey key) {
            this.quads.addAll(quads);
            this.key = key;
        }

        protected static final Model create(TrafficLightTextureKey key) {
            TextureAtlasSprite sprite = key.getSprite();
            List<BakedQuad> quads = new ArrayList<>();
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 16, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(0, 0, 0), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, pixel * 4), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(0, 0, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(0, pixel * 4, 0), 16, 1, Transformation.identity(), sprite));
        
            return new Model(quads, key);
        }

        private void render(PoseStack poseStack, VertexConsumer consumer) {
            quads.forEach(x -> consumer.putBulkData(poseStack.last(), x, 1, 1, 1, key.isOffState() ? LightTexture.FULL_BLOCK : LightTexture.FULL_BRIGHT, 0));        
        }
    }
}
