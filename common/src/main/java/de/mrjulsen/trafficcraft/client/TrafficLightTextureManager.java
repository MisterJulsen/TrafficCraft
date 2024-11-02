package de.mrjulsen.trafficcraft.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.ber.BERCube;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.data.Pair;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;

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
            return ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, String.format("textures/%s/off.png", TEXTURE_PATH));
        }
        return ResourceLocation.fromNamespaceAndPath(TrafficCraft.MOD_ID, String.format("textures/%s/%s_%s.png",
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
        private final BERCube cube;

        private TrafficLightBulbModel(TrafficLightTextureKey key) {
            this.key = key;
            if (key != null) {
                cube = BERCube.cube(key.getTextureLocation(), pixel * 4, pixel * 4, pixel, dir -> dir != Direction.NORTH && dir != Direction.UP, dir -> {
                    switch (dir) {
                        case WEST:
                        case EAST:
                            return Pair.of(new Vec2(0, 0), new Vec2(pixel, 1));
                        case DOWN:
                        case UP:
                            return Pair.of(new Vec2(0, 0), new Vec2(1, pixel));
                        default:
                            return Pair.of(new Vec2(0, 0), new Vec2(1, 1));
                    }
                });
            } else {
                cube = new BERCube(0, 0, 0);
            }
        }

        protected static final TrafficLightBulbModel create(TrafficLightTextureKey key) {
            return new TrafficLightBulbModel(key);
        }

        private void render(BERGraphics<?> graphics, BlockEntity be, int light) {
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, pixel);
            cube.setLight(key.isOffState() ? light : LightTexture.FULL_BRIGHT);
            cube.render(graphics);    
            graphics.poseStack().popPose();
        }

        private TrafficLightTextureKey getKey() {
            return key;
        }
    }
}
