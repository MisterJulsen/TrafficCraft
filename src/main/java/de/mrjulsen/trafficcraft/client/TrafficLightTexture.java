package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.type.ErrorType;

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

public enum TrafficLightTexture {
    OFF(0, "off", TrafficLightIcon.NONE, TrafficLightColor.NONE),
    RED(1, "normal_red", TrafficLightIcon.NONE, TrafficLightColor.RED),
    YELLOW(2, "normal_yellow", TrafficLightIcon.NONE, TrafficLightColor.YELLOW),
    GREEN(3, "normal_green", TrafficLightIcon.NONE, TrafficLightColor.GREEN),
    RIGHT_RED(4, "right_red", TrafficLightIcon.RIGHT, TrafficLightColor.RED),
    RIGHT_YELLOW(5, "right_yellow", TrafficLightIcon.RIGHT, TrafficLightColor.YELLOW),
    RIGHT_GREEN(6, "right_green", TrafficLightIcon.RIGHT, TrafficLightColor.GREEN),
    LEFT_RED(7, "left_red", TrafficLightIcon.LEFT, TrafficLightColor.RED),
    LEFT_YELLOW(8, "left_yellow", TrafficLightIcon.LEFT, TrafficLightColor.YELLOW),
    LEFT_GREEN(9, "left_green", TrafficLightIcon.LEFT, TrafficLightColor.GREEN),
    STRAIGHT_RED(10, "straight_red", TrafficLightIcon.STRAIGHT, TrafficLightColor.RED),
    STRAIGHT_YELLOW(11, "straight_yellow", TrafficLightIcon.STRAIGHT, TrafficLightColor.YELLOW),
    STRAIGHT_GREEN(12, "straight_green", TrafficLightIcon.STRAIGHT, TrafficLightColor.GREEN),
    STRAIGHT_RIGHT_RED(13, "straight_right_red", TrafficLightIcon.STRAIGHT_RIGHT, TrafficLightColor.RED),
    STRAIGHT_RIGHT_YELLOW(14, "straight_right_yellow", TrafficLightIcon.STRAIGHT_RIGHT, TrafficLightColor.YELLOW),
    STRAIGHT_RIGHT_GREEN(15, "straight_right_green", TrafficLightIcon.STRAIGHT_RIGHT, TrafficLightColor.GREEN),
    STRAIGHT_LEFT_RED(16, "straight_left_red", TrafficLightIcon.STRAIGHT_LEFT, TrafficLightColor.RED),
    STRAIGHT_LEFT_YELLOW(17, "straight_left_yellow", TrafficLightIcon.STRAIGHT_LEFT, TrafficLightColor.YELLOW),
    STRAIGHT_LEFT_GREEN(18, "straight_left_green", TrafficLightIcon.STRAIGHT_LEFT, TrafficLightColor.GREEN),
    PEDESTRIAN_RED(19, "pedestrian_red", TrafficLightIcon.PEDESTRIAN, TrafficLightColor.RED),
    PEDESTRIAN_YELLOW(20, "pedestrian_yellow", TrafficLightIcon.PEDESTRIAN, TrafficLightColor.YELLOW),
    PEDESTRIAN_GREEN(21, "pedestrian_green", TrafficLightIcon.PEDESTRIAN, TrafficLightColor.GREEN);

    private static final Map<TrafficLightTexture, Model> models = new HashMap<>();
    static {
        Arrays.stream(TrafficLightTexture.values()).forEach(x -> models.put(x, Model.create(x)));
    }

    private int index;
    private String name;
    private TrafficLightIcon icon;
    private TrafficLightColor color;

    private static final String TEXTURE_PATH = "block/traffic_light";

    private TrafficLightTexture(int index, String name, TrafficLightIcon icon, TrafficLightColor color) {
        this.index = index;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public String getTextureName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public TrafficLightIcon getIcon() {
        return icon;
    }

    public TrafficLightColor getColor() {
        return color;
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(ModMain.MOD_ID, TEXTURE_PATH + "/" + getTextureName());
    }

    public TextureAtlasSprite getSprite() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.getTextureLocation());
    }

    public static Optional<TrafficLightTexture> getTexture(TrafficLightIcon icon, TrafficLightColor color) {
        return Arrays.stream(TrafficLightTexture.values()).filter(x -> x.getIcon() == icon && x.getColor() == color).findFirst();
    }

    public static TrafficLightTexture getDirectionByIndex(int index) {
        return Arrays.stream(TrafficLightTexture.values()).filter(x -> x.getIndex() == index).findFirst().orElse(TrafficLightTexture.OFF);
	}

    public void render(PoseStack poseStack, VertexConsumer consumer) {
        models.get(this).render(poseStack, consumer);
    }

    public static class Model {
        private static final float pixel = 1.0F / 16.0F;

        private final List<BakedQuad> quads = new ArrayList<>();
        private final TrafficLightTexture texture;

        private Model(List<BakedQuad> quads, TrafficLightTexture texture) {
            this.quads.addAll(quads);
            this.texture = texture;
        }

        protected static final Model create(TrafficLightTexture texture) {
            TextureAtlasSprite sprite = texture.getSprite();
            List<BakedQuad> quads = new ArrayList<>();
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 16, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, 0), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(0, 0, 0), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, pixel * 4), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(0, pixel * 4, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, 0, 0), new Vector3f(pixel * 1, 0, 0), new Vector3f(pixel * 1, 0, pixel * 4), new Vector3f(0, 0, pixel * 4), 16, 1, Transformation.identity(), sprite));
            quads.add(ClientTools.createQuad(new Vector3f(0, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, pixel * 4), new Vector3f(pixel * 1, pixel * 4, 0), new Vector3f(0, pixel * 4, 0), 16, 1, Transformation.identity(), sprite));
        
            return new Model(quads, texture);
        }

        private void render(PoseStack poseStack, VertexConsumer consumer) {
            quads.forEach(x -> consumer.putBulkData(poseStack.last(), x, 1, 1, 1, texture == TrafficLightTexture.OFF ? LightTexture.FULL_BLOCK : LightTexture.FULL_BRIGHT, 0));        
        }
    }
}
