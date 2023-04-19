package de.mrjulsen.trafficcraft;

import com.mojang.logging.LogUtils;

import de.mrjulsen.trafficcraft.block.ModBlocks;
import de.mrjulsen.trafficcraft.block.colors.TintedTextures;
import de.mrjulsen.trafficcraft.block.entity.ModBlockEntities;
import de.mrjulsen.trafficcraft.item.ModItems;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModMain.MOD_ID)
public class ModMain
{
    public static final String MOD_ID = "trafficcraft";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModMain()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);

        ModBlocks.register(eventBus);
        ModItems.register(eventBus);

        ModBlockEntities.register(eventBus);

        NetworkManager.registerNetworkPackets();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        /* RENDER LAYERS */
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_SIGN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAINT_BUCKET.get(), RenderType.cutout());

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
            ModBlocks.TRAFFIC_BOLLARD.get()
        );

        /* REGISTER CUSTOM ITEM PROPERTIES */
        ItemProperties.register(ModItems.PAINT_BRUSH.get(), new ResourceLocation("paint"), (itemStack, world, entity, id) -> { 
            CompoundTag nbt = itemStack.getTag();
            if (nbt != null) {
                return nbt.getInt("paint");
            }
            return 0;
        });
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("Welcome to the TrafficCraft mod.");
    }
}
