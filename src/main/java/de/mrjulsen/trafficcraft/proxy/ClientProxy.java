package de.mrjulsen.trafficcraft.proxy;

import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.block.client.HouseNumberSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.block.client.StreetSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.block.client.TownSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.block.colors.TintedTextures;
import de.mrjulsen.trafficcraft.block.entity.ModBlockEntities;
import de.mrjulsen.trafficcraft.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

public class ClientProxy implements IProxy {

    @Override
    public void setup(FMLCommonSetupEvent event) {
        /* RENDER LAYERS */
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CIRCLE_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRIANGLE_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SQUARE_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.DIAMOND_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RECTANGLE_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MISC_TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAINT_BUCKET.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANHOLE_COVER.get(), RenderType.cutout());


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
        
        blockColors.register(new TintedTextures.BasicBlockTint(),
            blocks
        );
        
        ItemColors itemColors = Minecraft.getInstance().getItemColors();
        itemColors.register(new TintedTextures.BasicItemTint(),
            ModBlocks.GUARDRAIL.get(),
            ModItems.PAINT_BRUSH.get(),
            ModBlocks.TRAFFIC_CONE.get(),
            ModBlocks.TRAFFIC_BOLLARD.get(),
            ModBlocks.ROAD_BARRIER_FENCE.get(),
            ModBlocks.CONCRETE_BARRIER.get()
        );

        /* BLOCK ENTITY RENDERERS */
        BlockEntityRenderers.register(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), TownSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), StreetSignBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), HouseNumberSignBlockEntityRenderer::new);

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
