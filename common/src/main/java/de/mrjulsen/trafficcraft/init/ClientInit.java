package de.mrjulsen.trafficcraft.init;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.Wikipedia;
import de.mrjulsen.mcdragonlib.util.DLColor.ColorChannel;
import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.HouseNumberSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.client.TintedTextures;
import de.mrjulsen.trafficcraft.client.ber.TownSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficLightBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.TrafficSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.ber.WritableSignBlockEntityRenderer;
import de.mrjulsen.trafficcraft.client.screen.TrafficSignWorkbenchGui;
import de.mrjulsen.trafficcraft.client.screen.menu.ModMenuTypes;
import de.mrjulsen.trafficcraft.client.tooltip.ClientTrafficSignTooltipStack;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.item.IScrollEventItem;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.injectables.annotations.PlatformOnly;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ClientInit {

	private static final int CHECKERBOARD_COLOR_A = 0xFFE9E9E9;
	private static final int CHECKERBOARD_COLOR_B = 0xFFD9D9D9;
    private static final Map<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>> tooltipComponentFactories = new ConcurrentHashMap<>();


    /**
     * Register a factory for ClientTooltipComponents.
     * @param cls the class for the component
     * @param factory the factory for the ClientTooltipComponent
     */
    @PlatformOnly(value = PlatformOnly.FABRIC)
    @SuppressWarnings("unchecked")
    public static <T extends TooltipComponent> void registerTooltipComponentFactory(Class<T> cls, Function<? super T, ? extends ClientTooltipComponent> factory) {
        tooltipComponentFactories.put(cls, (Function<TooltipComponent, ClientTooltipComponent>) factory);
    }

    @PlatformOnly(value = PlatformOnly.FABRIC)
    @Nullable
    public static ClientTooltipComponent getClientTooltipComponent(TooltipComponent component) {
        var factory = tooltipComponentFactories.get(component.getClass());
        return factory == null ? null : factory.apply(component);
    }

    

    public static int[][] textureToIntArray(DynamicTexture tex, boolean flipRgb) {
        final int[][] a = new int[tex.getPixels().getWidth()][];
        for (int x = 0; x < tex.getPixels().getWidth(); x++) {
            a[x] = new int[tex.getPixels().getHeight()];
            for (int y = 0; y < tex.getPixels().getHeight(); y++) {
                a[x][y] = flipRgb ? DLColor.fromInt(tex.getPixels().getPixelRGBA(x, y)).swapChannels(ColorChannel.R, ColorChannel.B).getAsARGB() : tex.getPixels().getPixelRGBA(x, y);
            }
        }
        return a;
    }

    public static final DynamicTexture[] SHAPE_TEXTURES = new DynamicTexture[TrafficSignShape.values().length];

    @SuppressWarnings("unchecked")
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(mc -> {
            
            Wikipedia.addArticle(Constants.WIKIPEDIA_TRAFFIC_LIGHT_ID, Constants.WIKIPEDIA_GERMAN_TRAM_SIGNAL_ID);
            
            ItemModelGenerator.LAYERS.add("layer5");
            ItemModelGenerator.LAYERS.add("layer6");
            ItemModelGenerator.LAYERS.add("layer7");
            ItemModelGenerator.LAYERS.add("layer8");
            
            /* RENDER LAYERS */
            RenderTypeRegistry.register(RenderType.cutout(), 
                ModBlocks.PAINT_BUCKET.get(),
                ModBlocks.MANHOLE.get(),
                ModBlocks.MANHOLE_COVER.get(),
                ModBlocks.TRAFFIC_SIGN_WORKBENCH.get()
            );
            RenderTypeRegistry.register(RenderType.translucent(), 
                ModBlocks.ROAD_SALT.get()
            );
            RenderTypeRegistry.register(RenderType.cutout(), ModBlocks.COLORED_BLOCKS.stream().filter(x -> x.getId().toString().contains("pattern")).map(RegistrySupplier::get).toArray(Block[]::new));


            /* BLOCK ENTITY RENDERERS */
            BlockEntityRendererRegistry.register(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), TownSignBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.STREET_SIGN_BLOCK_ENTITY.get(), WritableSignBlockEntityRenderer<StreetSignBlockEntity>::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.HOUSE_NUMBER_SIGN_BLOCK_ENTITY.get(), WritableSignBlockEntityRenderer<HouseNumberSignBlockEntity>::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), TrafficSignBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.TRAFFIC_LIGHT_BLOCK_ENTITY.get(), TrafficLightBlockEntityRenderer::new);
            
            if (Platform.isFabric()) {
                ClientInit.registerTooltipComponentFactory(TrafficSignTooltip.class, (tooltip) -> {
                    return new ClientTrafficSignTooltipStack(tooltip);
                });
            }

            /* REGISTER MENUS */
            MenuScreens.register(ModMenuTypes.TRAFFIC_SIGN_WORKBENCH_MENU.get(), TrafficSignWorkbenchGui::new);

            /* REGISTER CUSTOM ITEM PROPERTIES */

            ItemPropertiesRegistry.register(ModItems.PAINT_BRUSH.get(), new ResourceLocation(TrafficCraft.MOD_ID, "paint"), (itemStack, world, entity, id) -> { 
                CompoundTag nbt = itemStack.getTag();
                if (nbt != null) {
                    return nbt.getInt("paint");
                }
                return 0;
            });

            ItemPropertiesRegistry.register(ModItems.TRAFFIC_LIGHT_LINKER.get(), new ResourceLocation(TrafficCraft.MOD_ID, "mode"), (itemStack, world, entity, id) -> { 
                CompoundTag nbt = itemStack.getTag();
                if (nbt != null) {
                    return nbt.getInt("Mode");
                }
                return 0;
            });
        });

        /* BLOCK COLORS */

        ClientLifecycleEvent.CLIENT_STARTED.register(mc -> {
            ColorHandlerRegistry.registerBlockColors(new TintedTextures.TintedBlock(), ModBlocks.COLORED_BLOCKS.toArray(RegistrySupplier[]::new));
            ColorHandlerRegistry.registerItemColors(new TintedTextures.TintedItem(), 
                ModBlocks.GUARDRAIL,
                ModItems.PAINT_BRUSH,
                ModBlocks.TRAFFIC_CONE,
                ModBlocks.TRAFFIC_BOLLARD,
                ModBlocks.TRAFFIC_BARREL,
                ModBlocks.ROAD_BARRIER_FENCE,
                ModBlocks.CONCRETE_BARRIER,
                ModItems.COLOR_PALETTE
            );

            DynamicTexture[] textures = Arrays.stream(TrafficSignShape.values()).map(v -> {
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
            for (int i = 0; i < textures.length; i++) {
                SHAPE_TEXTURES[i] = textures[i];
            }
        });

        ClientTickEvent.CLIENT_LEVEL_POST.register(level -> {            
            RoadConstructionTool.clientTick();
        });

        ClientRawInputEvent.MOUSE_SCROLLED.register((mc, delta) -> {
            LocalPlayer player = mc.player;

            if (player == null || delta == 0) {
                return EventResult.pass();
            }

            ItemStack stack = player.getMainHandItem() == null ? player.getOffhandItem() : player.getMainHandItem();
            if (stack != null && stack.getItem() instanceof IScrollEventItem item) {
                if (item.mouseScroll(player, stack, delta)) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        ClientGuiEvent.DEBUG_TEXT_LEFT.register(list -> {
            list.add(String.format("TC | T: %s",
                TrafficSignClientTexture.debug_cachedTexturesCount()
            ));
        });
        
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            TrafficCraft.LOGGER.info("Cleaning up traffic sign texture cache...");
            int count = TrafficSignClientTexture.closeAll();
            TrafficCraft.LOGGER.info("All " + count + " loaded custom traffic sign textures have been closed.");
        });
    }
    
}
