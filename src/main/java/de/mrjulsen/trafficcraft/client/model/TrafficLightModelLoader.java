package de.mrjulsen.trafficcraft.client.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class TrafficLightModelLoader implements IModelLoader<TrafficLightModelLoader.GeneratorModelGeometry> {

    public static final ResourceLocation GENERATOR_LOADER = new ResourceLocation(ModMain.MOD_ID, "generatorloader");

    public static final ResourceLocation GENERATOR_FRONT_POWERED = new ResourceLocation(ModMain.MOD_ID, "block/generator_front_powered");
    public static final ResourceLocation GENERATOR_FRONT = new ResourceLocation(ModMain.MOD_ID, "block/generator_front");
    public static final ResourceLocation GENERATOR_SIDE = new ResourceLocation(ModMain.MOD_ID, "block/generator_side");
    public static final ResourceLocation GENERATOR_ON = new ResourceLocation(ModMain.MOD_ID, "block/generator_on");
    public static final ResourceLocation GENERATOR_OFF = new ResourceLocation(ModMain.MOD_ID, "block/generator_off");

    public static final Material MATERIAL_FRONT_POWERED = ForgeHooksClient.getBlockMaterial(GENERATOR_FRONT_POWERED);
    public static final Material MATERIAL_FRONT = ForgeHooksClient.getBlockMaterial(GENERATOR_FRONT);
    public static final Material MATERIAL_SIDE = ForgeHooksClient.getBlockMaterial(GENERATOR_SIDE);
    public static final Material MATERIAL_ON = ForgeHooksClient.getBlockMaterial(GENERATOR_ON);
    public static final Material MATERIAL_OFF = ForgeHooksClient.getBlockMaterial(GENERATOR_OFF);

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public GeneratorModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new GeneratorModelGeometry();
    }


    public static class GeneratorModelGeometry implements IModelGeometry<GeneratorModelGeometry> {

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return null;//return new TrafficLightModel(modelTransform, spriteGetter, overrides, owner.getCameraTransforms());
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return List.of(MATERIAL_FRONT, MATERIAL_FRONT_POWERED, MATERIAL_SIDE, MATERIAL_ON, MATERIAL_OFF);
        }
    }
}
