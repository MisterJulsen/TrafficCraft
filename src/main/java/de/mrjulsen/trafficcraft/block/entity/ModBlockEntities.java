package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ModMain.MOD_ID);


    public static final RegistryObject<BlockEntityType<TrafficLightBlockEntity>> TRAFFIC_LIGHT_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_block_entity", () -> BlockEntityType.Builder.of(TrafficLightBlockEntity::new, ModBlocks.TRAFFIC_LIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrafficLightControllerBlockEntity>> TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_controller_block_entity", () -> BlockEntityType.Builder.of(TrafficLightControllerBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrafficLightRequestButtonBlockEntity>> TRAFFIC_LIGHT_REQUEST_BUTTON_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_request_button_block_entity", () -> BlockEntityType.Builder.of(TrafficLightRequestButtonBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_REQUEST_BUTTON.get()).build(null));
    public static final RegistryObject<BlockEntityType<ColoredBlockEntity>> COLORED_BLOCK_ENTITY = BLOCK_ENTITIES.register("colored_block_entity", () -> BlockEntityType.Builder.of(ColoredBlockEntity::new, ModBlocks.GUARDRAIL.get()).build(null));    
    public static final RegistryObject<BlockEntityType<StreetLampBlockEntity>> STREET_LAMP_BLOCK_ENTITY = BLOCK_ENTITIES.register("street_lamp_block_entity", () -> BlockEntityType.Builder.of(StreetLampBlockEntity::new, ModBlocks.STREET_LAMP.get(), ModBlocks.DOUBLE_STREET_LAMP.get(), ModBlocks.SMALL_STREET_LAMP.get(), ModBlocks.SMALL_DOUBLE_STREET_LAMP.get()).build(null));
    public static final RegistryObject<BlockEntityType<TownSignBlockEntity>> TOWN_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("town_sign_block_entity", () -> BlockEntityType.Builder.of(TownSignBlockEntity::new, ModBlocks.TOWN_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<StreetSignBlockEntity>> STREET_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("street_sign_block_entity", () -> BlockEntityType.Builder.of(StreetSignBlockEntity::new, ModBlocks.STREET_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<HouseNumberSignBlockEntity>> HOUSE_NUMBER_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("house_number_sign_block_entity", () -> BlockEntityType.Builder.of(HouseNumberSignBlockEntity::new, ModBlocks.HOUSE_NUMBER_SIGN.get()).build(null));

    /*
    static
    {
        Block[] blocks = new Block[ModBlocks.COLORED_BLOCKS.size()];
        for (int i = 0; i < ModBlocks.COLORED_BLOCKS.size(); i++) {
            blocks[i] = ModBlocks.COLORED_BLOCKS.get(i).get();
        }

        COLORED_BLOCK_ENTITY = BLOCK_ENTITIES.register("colored_block_entity", () -> BlockEntityType.Builder.of(ColoredBlockEntity::new, blocks).build(null));    
    }
    */

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
