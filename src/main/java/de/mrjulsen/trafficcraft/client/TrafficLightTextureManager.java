package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import de.mrjulsen.mcdragonlib.utils.ClientTools;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class TrafficLightTextureManager {
    private static final Model FALLBACK_MODEL = Model.create(null);
    private static final String TEXTURE_PATH = "block/traffic_light";
    private static final List<Model> models = new ArrayList<>();

    static {
        Arrays.stream(TrafficLightIcon.values())
            .forEach(
                x -> Arrays.stream(TrafficLightColor.values())
                    .filter(y -> x.isApplicableToColor(y))
                    .forEach(y -> {
                        TrafficLightTextureKey key = new TrafficLightTextureKey(x, y);
                        models.add(Model.create(key));
                    }));
    }


    public static ResourceLocation getResourceLocation(TrafficLightIcon icon, TrafficLightColor color) {
        return getResourceLocation(new TrafficLightTextureKey(icon, color));
    }

    public static ResourceLocation getResourceLocation(TrafficLightTextureKey key) {
        if (key.isOffState()) {
            return new ResourceLocation(ModMain.MOD_ID, String.format("%s/off", TEXTURE_PATH));
        }
        return new ResourceLocation(ModMain.MOD_ID, String.format("%s/%s_%s",
            TEXTURE_PATH,
            (key.getIcon().isApplicableToColor(key.getColor()) ? key.getIcon() : TrafficLightIcon.NONE).getName(),
            key.getColor().getName()
        ));
    }

    public static ResourceLocation getTextureLocation(TrafficLightTextureKey key) {
        if (key.isOffState()) {
            return new ResourceLocation(ModMain.MOD_ID, String.format("textures/%s/off.png", TEXTURE_PATH));
        }
        return new ResourceLocation(ModMain.MOD_ID, String.format("textures/%s/%s_%s.png",
            TEXTURE_PATH,
            (key.getIcon().isApplicableToColor(key.getColor()) ? key.getIcon() : TrafficLightIcon.NONE).getName(),
            key.getColor().getName()
        ));
    }

    public static Collection<ResourceLocation> getAllTextureLocations() {
        return models.stream().map(x -> x.getKey().getTextureLocation()).toList();
    }

    public static TextureAtlasSprite getSprite(TrafficLightIcon icon, TrafficLightColor color) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getResourceLocation(icon, color));
    }

    public static void render(PoseStack poseStack, VertexConsumer consumer, TrafficLightIcon icon, TrafficLightColor color, int packedLight, int packedOverlay) {
        render(poseStack, consumer, new TrafficLightTextureKey(icon, color), packedLight, packedOverlay);
    }

    public static void render(PoseStack poseStack, VertexConsumer consumer, TrafficLightTextureKey key, int packedLight, int packedOverlay) {
        models.stream().filter(x -> x.getKey().equals(key)).findFirst().orElse(FALLBACK_MODEL).render(poseStack, consumer, packedLight, packedOverlay);
    }

    public static class TrafficLightTextureKey {
        private final TrafficLightIcon icon;
        private final TrafficLightColor color;

        public TrafficLightTextureKey(TrafficLightIcon icon, TrafficLightColor color) {
            this.color = color;
            this.icon = icon.isApplicableToColor(color) ? icon : TrafficLightIcon.NONE;
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
                return getColor() == other.getColor() && getIcon() == other.getIcon();
            }
            return false;
        }

        public ResourceLocation getTextureLocation() {
            return TrafficLightTextureManager.getResourceLocation(getIcon(), getColor());
        }

        public TextureAtlasSprite getSprite() {
            return TrafficLightTextureManager.getSprite(getIcon(), getColor());
        }

        public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
            TrafficLightTextureManager.render(poseStack, consumer, this, packedLight, packedOverlay);
        }

        public boolean isOffState() {
            return getIcon() == TrafficLightIcon.NONE && getColor() == TrafficLightColor.NONE;
        }
    }

    private static class Model {
        private static final float pixel = 1.0F / 16.0F;

        private final List<BakedQuad> quads = new ArrayList<>();
        private final TrafficLightTextureKey key;

        private Model(List<BakedQuad> quads, TrafficLightTextureKey key) {
            this.quads.addAll(quads);
            this.key = key;
        }

        protected static final Model create(TrafficLightTextureKey key) {
            TextureAtlasSprite sprite = key == null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TextureManager.INTENTIONAL_MISSING_TEXTURE) : key.getSprite();
            List<BakedQuad> quads = new ArrayList<>();
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 16, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(0, 0, 0), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, pixel * 4), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(0, 0, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(0, pixel * 4, 0), 16, 1, Transformation.identity(), sprite));
        
            return new Model(quads, key);
        }

        private void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
            quads.forEach(x -> consumer.putBulkData(poseStack.last(), x, 1, 1, 1, key == null || key.isOffState() ? packedLight : LightTexture.FULL_BRIGHT, packedOverlay));        
        }

        private TrafficLightTextureKey getKey() {
            return key;
        }
    }
}
