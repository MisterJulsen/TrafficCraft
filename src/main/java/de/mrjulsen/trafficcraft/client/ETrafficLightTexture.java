package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.util.ClientTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public enum ETrafficLightTexture {
    OFF("off"),
    RED("normal_red"),
    YELLOW("normal_yellow"),
    GREEN("normal_green"),
    RIGHT_RED("right_red"),
    RIGHT_YELLOW("right_yellow"),
    RIGHT_GREEN("right_green"),
    LEFT_RED("left_red"),
    LEFT_YELLOW("left_yellow"),
    LEFT_GREEN("left_green"),
    STRAIGHT_RED("straight_red"),
    STRAIGHT_YELLOW("straight_yellow"),
    STRAIGHT_GREEN("straight_green"),
    STRAIGHT_RIGHT_RED("straight_right_red"),
    STRAIGHT_RIGHT_YELLOW("straight_right_yellow"),
    STRAIGHT_RIGHT_GREEN("straight_right_green"),
    STRAIGHT_LEFT_RED("straight_left_red"),
    STRAIGHT_LEFT_YELLOW("straight_left_yellow"),
    STRAIGHT_LEFT_GREEN("straight_left_green"),
    PEDESTRIAN_RED("pedestrian_red"),
    PEDESTRIAN_YELLOW("pedestrian_yellow"),
    PEDESTRIAN_GREEN("pedestrian_green");

    private static final Map<ETrafficLightTexture, Model> models = new HashMap<>();
    static {
        Arrays.stream(ETrafficLightTexture.values()).forEach(x -> models.put(x, Model.create(x)));
    }

    private String name;

    private static final String TEXTURE_PATH = "block/traffic_light";

    private ETrafficLightTexture(String name) {
        this.name = name;
    }

    public String getTextureName() {
        return name;
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(ModMain.MOD_ID, TEXTURE_PATH + "/" + getTextureName());
    }

    public TextureAtlasSprite getSprite() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.getTextureLocation());
    }

    public void render(PoseStack poseStack, VertexConsumer consumer) {
        models.get(this).render(poseStack, consumer);
    }

    public static class Model {
        private final List<BakedQuad> quads = new ArrayList<>();
        private static final float pixel = 1.0F / 16.0F;

        private Model(List<BakedQuad> quads) {
            this.quads.addAll(quads);
        }

        protected static final Model create(ETrafficLightTexture texture) {
            TextureAtlasSprite sprite = texture.getSprite();
            List<BakedQuad> quads = new ArrayList<>();
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 16, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(0, 0, 0), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, pixel * 4), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(0, 0, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(0, pixel * 4, 0), 16, 1, Transformation.identity(), sprite));
        
            return new Model(quads);
        }

        private void render(PoseStack poseStack, VertexConsumer consumer) {
            quads.forEach(x -> consumer.putBulkData(poseStack.last(), x, 1, 1, 1, LightTexture.FULL_BRIGHT, 0));        
        }
    }
}
