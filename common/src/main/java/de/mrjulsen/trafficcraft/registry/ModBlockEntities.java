package de.mrjulsen.trafficcraft.registry;

import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.HouseNumberSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.EmptyBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetLampBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightRequestButtonBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(TrafficCraft.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<TrafficLightBlockEntity>> TRAFFIC_LIGHT_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_block_entity", () -> BlockEntityType.Builder.of(TrafficLightBlockEntity::new, ModBlocks.TRAFFIC_LIGHT.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<TrafficLightControllerBlockEntity>> TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_controller_block_entity", () -> BlockEntityType.Builder.of(TrafficLightControllerBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<TrafficLightRequestButtonBlockEntity>> TRAFFIC_LIGHT_REQUEST_BUTTON_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_request_button_block_entity", () -> BlockEntityType.Builder.of(TrafficLightRequestButtonBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_REQUEST_BUTTON.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<ColoredBlockEntity>> COLORED_BLOCK_ENTITY = BLOCK_ENTITIES.register("colored_block_entity", () -> BlockEntityType.Builder.of(ColoredBlockEntity::new,
        ModBlocks.COLORED_BLOCKS.stream().map(RegistrySupplier::get).toArray(Block[]::new)
    ).build(null));  
    public static final RegistrySupplier<BlockEntityType<EmptyBlockEntity>> EMPTY_BLOCK_ENTITY = BLOCK_ENTITIES.register("road_salt_block_entity", () -> BlockEntityType.Builder.of(EmptyBlockEntity::new, ModBlocks.ROAD_SALT.get()).build(null));    
    public static final RegistrySupplier<BlockEntityType<StreetLampBlockEntity>> STREET_LAMP_BLOCK_ENTITY = BLOCK_ENTITIES.register("street_lamp_block_entity", () -> BlockEntityType.Builder.of(StreetLampBlockEntity::new,
        ModBlocks.STREET_LAMP.get(),
        ModBlocks.DOUBLE_STREET_LAMP.get(),
        ModBlocks.SMALL_STREET_LAMP.get(),
        ModBlocks.SMALL_DOUBLE_STREET_LAMP.get(),
        ModBlocks.STREET_LIGHT.get(),
        ModBlocks.FLUORESCENT_TUBE_LAMP.get()
    ).build(null));
    public static final RegistrySupplier<BlockEntityType<TownSignBlockEntity>> TOWN_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("town_sign_block_entity", () -> BlockEntityType.Builder.of(TownSignBlockEntity::new, ModBlocks.TOWN_SIGN.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<StreetSignBlockEntity>> STREET_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("street_sign_block_entity", () -> BlockEntityType.Builder.of(StreetSignBlockEntity::new, ModBlocks.STREET_SIGN.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<HouseNumberSignBlockEntity>> HOUSE_NUMBER_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("house_number_sign_block_entity", () -> BlockEntityType.Builder.of(HouseNumberSignBlockEntity::new, ModBlocks.HOUSE_NUMBER_SIGN.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<TrafficSignBlockEntity>> TRAFFIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_sign_block_entity", () -> BlockEntityType.Builder.of(TrafficSignBlockEntity::new, ModBlocks.TRAFFIC_SIGN.get()).build(null));
        
    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
