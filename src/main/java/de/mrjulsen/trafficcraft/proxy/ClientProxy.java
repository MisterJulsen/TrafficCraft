package de.mrjulsen.trafficcraft.proxy;

import java.util.Arrays;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.client.TintedTextures;
import de.mrjulsen.trafficcraft.client.ber.HouseNumberSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.StreetSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TownSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficLightBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.client.tooltip.ClientTrafficSignTooltipStack;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

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
        
        ItemModelGenerator.LAYERS.add("layer5");
        ItemModelGenerator.LAYERS.add("layer6");
        ItemModelGenerator.LAYERS.add("layer7");
        ItemModelGenerator.LAYERS.add("layer8");
        
        /* RENDER LAYERS */
        /*
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_LIGHT.get(), RenderTypes.getGlowingSolid());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAINT_BUCKET.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE_COVER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_SIGN_WORKBENCH.get(), RenderType.cutout());
        */

        for (RegistryObject<Block> block : ModBlocks.COLORED_BLOCKS) {
            if (block.getId().toString().contains("pattern")) {
                ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
            }
        }

        /* BLOCK COLORS */
        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        Block[] blocks = new Block[ModBlocks.COLORED_BLOCKS.size()];
        for (int i = 0; i < ModBlocks.COLORED_BLOCKS.size(); i++) {
            blocks[i] = ModBlocks.COLORED_BLOCKS.get(i).get();
        }
        
        blockColors.register(new TintedTextures.TintedBlock(),
            blocks
        );
        
        ItemColors itemColors = Minecraft.getInstance().getItemColors();
        itemColors.register(new TintedTextures.TintedItem(),
            ModBlocks.GUARDRAIL.get(),
            ModItems.PAINT_BRUSH.get(),
            ModBlocks.TRAFFIC_CONE.get(),
            ModBlocks.TRAFFIC_BOLLARD.get(),
            ModBlocks.TRAFFIC_BARREL.get(),
            ModBlocks.ROAD_BARRIER_FENCE.get(),
            ModBlocks.CONCRETE_BARRIER.get(),
            ModItems.COLOR_PALETTE.get()
        );

        /* BLOCK ENTITY RENDERERS */
        BlockEntityRenderers.register(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), TownSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), StreetSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), HouseNumberSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), TrafficSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), TrafficLightBlockEntityRenderer::new);

        MinecraftForgeClient.registerTooltipComponentFactory(TrafficSignTooltip.class, (tooltip) -> {
            return new ClientTrafficSignTooltipStack(tooltip);
        });

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
    }
    
}
