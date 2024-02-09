package de.mrjulsen.trafficcraft.init;

import java.util.Arrays;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.mcdragonlib.utils.Wikipedia;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.HouseNumberSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.TownSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficLightBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.WritableSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit {

	private static final int CHECKERBOARD_COLOR_A = 0xFFE9E9E9;
	private static final int CHECKERBOARD_COLOR_B = 0xFFD9D9D9;

    public static final DynamicTexture[] SHAPE_TEXTURES = Arrays.stream(TrafficSignShape.values()).map(v -> {
		NativeImage image = new NativeImage(NativeImage.Format.RGBA, TrafficSignShape.MAX_WIDTH, TrafficSignShape.MAX_HEIGHT, false);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (v.isPixelValid(x, y)) {
					image.setPixelRGBA(x, y, x % 2 == 0 ? (y % 2 == 0 ? CHECKERBOARD_COLOR_A : CHECKERBOARD_COLOR_B) : (y % 2 == 0 ? CHECKERBOARD_COLOR_B : CHECKERBOARD_COLOR_A));
				} else {
                    image.setPixelRGBA(x, y, 0);
                }
			}
		}
		return new DynamicTexture(image);
	}).toArray(DynamicTexture[]::new);

    public static void setup(final FMLClientSetupEvent event) {
                
        Wikipedia.addArticle(Constants.WIKIPEDIA_TRAFFIC_LIGHT_ID, Constants.WIKIPEDIA_GERMAN_TRAM_SIGNAL_ID);

        ItemModelGenerator.LAYERS.add("layer5");
        ItemModelGenerator.LAYERS.add("layer6");
        ItemModelGenerator.LAYERS.add("layer7");
        ItemModelGenerator.LAYERS.add("layer8");

        /* BLOCK ENTITY RENDERERS */
        BlockEntityRenderers.register(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), TownSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), WritableSignBlockEntityRenderer<StreetSignBlockEntity>::new);
        BlockEntityRenderers.register(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), WritableSignBlockEntityRenderer<HouseNumberSignBlockEntity>::new);
        BlockEntityRenderers.register(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), TrafficSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), TrafficLightBlockEntityRenderer::new);
        
        /* REGISTER MENUS */
        MenuScreens.register(ModMenuTypes.TRAFFIC_SIGN_WORKBENCH_MENU.get(), TrafficSignWorkbenchGui::new);

        /* REGISTER CUSTOM ITEM PROPERTIES */
        ItemProperties.register(ModItems.PAINT_BRUSH.get(), new ResourceLocation("paint"), (itemStack, world, entity, id) -> { 
            CompoundTag nbt = itemStack.getTag();
            if (nbt != null) {
                return nbt.getInt("paint");
            }
            return 0;
        });

        ItemProperties.register(ModItems.TRAFFIC_LIGHT_LINKER.get(), new ResourceLocation("mode"), (itemStack, world, entity, id) -> { 
            CompoundTag nbt = itemStack.getTag();
            if (nbt != null) {
                return nbt.getInt("Mode");
            }
            return 0;
        });
    }
    
}
