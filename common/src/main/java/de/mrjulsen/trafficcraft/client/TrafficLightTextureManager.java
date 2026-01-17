package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.model.mesh.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficLightTextureManager {
    private static final TrafficLightBulbModel FALLBACK_MODEL = TrafficLightBulbModel.create(null);
    private static final String TEXTURE_PATH = "block/traffic_light";
    private static final List<TrafficLightBulbModel> models = new ArrayList<>();

    static {
        Arrays.stream(TrafficLightIcon.values())
            .forEach(
                x -> Arrays.stream(TrafficLightColor.values())
                    .filter(y -> x.isApplicableToColor(y))
                    .forEach(y -> {
                        TrafficLightTextureKey key = new TrafficLightTextureKey(x, y);
                        models.add(TrafficLightBulbModel.create(key));
                    }));
    }


    public static ResourceLocation getResourceLocation(TrafficLightIcon icon, TrafficLightColor color) {
        return getResourceLocation(new TrafficLightTextureKey(icon, color));
    }

    public static ResourceLocation getResourceLocation(TrafficLightTextureKey key) {
        if (key.isOffState()) {
            return new ResourceLocation(TrafficCraft.MOD_ID, String.format("textures/%s/off.png", TEXTURE_PATH));
        }
        return new ResourceLocation(TrafficCraft.MOD_ID, String.format("textures/%s/%s_%s.png",
            TEXTURE_PATH,
            (key.getIcon().isApplicableToColor(key.getColor()) ? key.getIcon() : TrafficLightIcon.NONE).getName(),
            key.getColor().getName()
        ));
    }

    public static Collection<ResourceLocation> getAllTextureLocations() {
        return models.stream().map(x -> x.getKey().getTextureLocation()).toList();
    }

    public static void render(BERGraphics<?> graphics, BlockEntity be, TrafficLightIcon icon, TrafficLightColor color, int packedLight) {
        render(graphics, be, new TrafficLightTextureKey(icon, color), packedLight);
    }

    public static void render(BERGraphics<?> graphics, BlockEntity be, TrafficLightTextureKey key, int packedLight) {
        models.stream().filter(x -> x.getKey().equals(key)).findFirst().orElse(FALLBACK_MODEL).render(graphics, be, packedLight);
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

        public void render(BERGraphics<?> graphics, BlockEntity be, int packedLight) {
            TrafficLightTextureManager.render(graphics, be, this, packedLight);
        }

        public boolean isOffState() {
            return getIcon() == TrafficLightIcon.NONE && getColor() == TrafficLightColor.NONE;
        }
    }

    private static class TrafficLightBulbModel {
        private static final float pixel = 1.0F / 16.0F;

        private final TrafficLightTextureKey key;
        private final BasicMesh cube;

        private TrafficLightBulbModel(TrafficLightTextureKey key) {
            this.key = key;

            if (key != null) {
                cube = new BasicMesh();
                Face frontFace = Face.createFace(Direction.SOUTH, new Vector3f(0, 0, pixel * 1), pixel * 4, pixel * 4);
                frontFace.setTexture(key.getTextureLocation());
                cube.addFace(frontFace);

                Face rightSide = Face.createFace(Direction.EAST, new Vector3f(pixel * 4, 0, 0), pixel * 1, pixel * 4);
                rightSide.getCorner(CornerType.TOP_RIGHT).setU(DragonLib.BLOCK_PIXEL);
                rightSide.getCorner(CornerType.BOTTOM_RIGHT).setU(DragonLib.BLOCK_PIXEL);
                rightSide.setTexture(key.getTextureLocation());
                cube.addFace(rightSide);

                Face leftSide = Face.createFace(Direction.WEST, new Vector3f(), pixel * 1, pixel * 4);
                leftSide.setTexture(key.getTextureLocation());
                leftSide.getCorner(CornerType.TOP_RIGHT).setU(DragonLib.BLOCK_PIXEL);
                leftSide.getCorner(CornerType.BOTTOM_RIGHT).setU(DragonLib.BLOCK_PIXEL);
                cube.addFace(leftSide);

                Face bottomFace = Face.createFace(Direction.DOWN, new Vector3f(), pixel * 4, pixel * 1);
                bottomFace.setTexture(key.getTextureLocation());
                bottomFace.getCorner(CornerType.BOTTOM_LEFT).setV(DragonLib.BLOCK_PIXEL);
                bottomFace.getCorner(CornerType.BOTTOM_RIGHT).setV(DragonLib.BLOCK_PIXEL);
                cube.addFace(bottomFace);

                cube.cleanUp();
            } else {
                cube = new BasicMesh();
            }
        }

        protected static final TrafficLightBulbModel create(TrafficLightTextureKey key) {
            return new TrafficLightBulbModel(key);
        }

        private void render(BERGraphics<?> graphics, BlockEntity be, int light) {
            graphics.poseStack().pushPose();
            //graphics.poseStack().translate(0, 0, pixel);
            //cube.setLight(key.isOffState() ? light : LightTexture.FULL_BRIGHT);
            cube.render(graphics, key.isOffState() ? graphics.packedLight() : LightTexture.FULL_BRIGHT, graphics.packedOverlay(), false, true);    
            graphics.poseStack().popPose();
        }

        private TrafficLightTextureKey getKey() {
            return key;
        }
    }
}
